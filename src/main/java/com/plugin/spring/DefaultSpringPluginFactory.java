package com.plugin.spring;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;
import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 12130
 * @date 2019/11/12
 * @time 12:49
 * <p>
 * 默认的插件工厂实现
 * 实现插件的下载安装激活删除
 * 插件的实现原理是利用Spring的AOP，所以我们的插件必须要实现Advice接口，才能添加到AOP中
 */
@Component
public class DefaultSpringPluginFactory implements SpringPluginFactory {
    /**
     * 插件是否被初始化，用来在程序启动的时候恢复之前的插件配置
     */
    private boolean init = false;
    @Value("${plugin.JarPath}")
    private String PLUGIN_JAR_PATH;
    @Value("${plugin.ConfigPath}")
    private String PLUGIN_CONFIG_PATH;
    @Value("${plugin.ConfigFileName}")
    private String PLUGIN_CONFIG_FILE_NAME;
    private Map<String, Advice> adviceMap = new HashMap<>();
    private Map<String, PluginConfig> config = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void installPlugin(PluginConfig pluginConfig) {
        if (config.containsKey(pluginConfig.getId())) {
            throw new RuntimeException(String.format("已存在指定插件 id=%s", pluginConfig.getId()));
        }
        config.put(pluginConfig.getId(), pluginConfig);
        try {
            buildAdvice(pluginConfig);
        } catch (Exception e) {
            config.remove(pluginConfig.getId());
            throw new RuntimeException(String.format("插件构建失败 id=%s", pluginConfig.getId()));
        }
        // 持久化到本地文件，下次启动自动加载
        try {
            saveConfigLocal();
        } catch (IOException e) {
            throw new RuntimeException("插件配置信息持久化异常");
        }
    }

    @Override
    public void enablePlugin(String pluginId) {
        if (!adviceMap.containsKey(pluginId)) {
            throw new RuntimeException("不存在该插件 id:" + pluginId);
        }
        String[] beNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beNames) {
            Object bean = applicationContext.getBean(beanName);
            // 这里默认是给所有的AOP对象加上插件，当然可以实现给指定的对象添加
            if (bean instanceof Advised) {
                Advised advised = (Advised) bean;
                advised.addAdvice(adviceMap.get(pluginId));
                config.get(pluginId).setActive(true);
            }
        }
        try {
            saveConfigLocal();
        } catch (IOException e) {
            throw new RuntimeException("插件配置信息持久化异常");
        }
    }

    @Override
    public void disablePlugin(String pluginId) {
        if (!adviceMap.containsKey(pluginId)) {
            throw new RuntimeException("不存在该插件 id:" + pluginId);
        }
        String[] beNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beNames) {
            Object bean = applicationContext.getBean(beanName);
            if (bean instanceof Advised) {
                Advised advised = (Advised) bean;
                advised.removeAdvice(adviceMap.get(pluginId));
            }
        }
        try {
            saveConfigLocal();
        } catch (IOException e) {
            throw new RuntimeException("插件配置信息持久化异常");
        }
    }

    @Override
    public void removePlugin(String pluginId) {
        // TODO 删除插件，包含删除插件的jar文件
        PluginConfig pluginConfig = config.get(pluginId);
        if (pluginConfig.isActive()) {
            disablePlugin(pluginId);
        }
        try {
            saveConfigLocal();
        } catch (IOException e) {
            throw new RuntimeException("插件配置信息持久化异常");
        }
    }

    /**
     * 构建通知，也就是将jar包加载进JVM
     *
     * @param config
     * @return
     */
    public Advice buildAdvice(PluginConfig config) throws Exception {
        // 先尝试从本地解析jar包
        File localFile = loadLocalFile(config);
        if (!localFile.exists()) {
            // 从远程解析jar包
            URL url = new URL(config.getId());
            InputStream inputStream = url.openStream();
            localFile.getParentFile().mkdirs();
            try {
                Files.copy(inputStream, localFile.toPath());
                // 更新配置文件的url到本地的文件
                config.setUrl(localFile.toURI().toURL());
            } catch (IOException e) {
                localFile.deleteOnExit();
                throw new RuntimeException("jar文件加载失败");
            }
            inputStream.close();
        }
        URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
        URL url = localFile.toURI().toURL();
        boolean isLoader = false;
        for (URL u : classLoader.getURLs()) {
            if (u.equals(url)) {
                isLoader = true;
                break;
            }
        }
        if (!isLoader) {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            // 加载jar
            method.invoke(classLoader, url);
        }
        Class<?> aClass = classLoader.loadClass(config.getClassName());
        if (!Advice.class.isAssignableFrom(aClass)) {
            throw new RuntimeException("插件非Advice的实现类");
        }
        Object o = aClass.newInstance();
        adviceMap.put(config.getId(), (Advice) o);
        return (Advice) o;
    }

    private File loadLocalFile(PluginConfig config) throws Exception {
        URL url = config.getUrl();
        if ("file".equals(url.getProtocol())) {
            // 是一个本地文件，直接返回就可以了
            return new File(url.toURI());
        } else {
            // 不是一个本地文件而是一个远程仓库的文件，要将远程仓库的文件下载下来
            String path = url.getPath();
            // 创建远程仓库对应到本地的file
            return new File(PLUGIN_JAR_PATH + path.substring(path.lastIndexOf("/")));
        }
    }

    public void saveConfigLocal() throws IOException {
        File file = new File(PLUGIN_CONFIG_PATH + PLUGIN_CONFIG_FILE_NAME);
        file.getParentFile().mkdirs();
        file.createNewFile();
        BufferedWriter fbout = new BufferedWriter(new FileWriter(file));
        JSONWriter jsonWriter = new JSONWriter(fbout);
        jsonWriter.startArray();
        for (Map.Entry<String, PluginConfig> entry : config.entrySet()) {
            jsonWriter.writeObject(entry.getValue());
        }
        jsonWriter.endArray();
        jsonWriter.close();
    }

    private void reloadConfigLocal() throws Exception {
        File file = new File(PLUGIN_CONFIG_PATH + PLUGIN_CONFIG_FILE_NAME);
        if (!file.exists()) {
            return;
        }
        BufferedReader fread = new BufferedReader(new FileReader(file));
        JSONReader jsonReader = new JSONReader(fread);
        jsonReader.startArray();
        while (jsonReader.hasNext()) {
            PluginConfig pluginConfig = jsonReader.readObject(PluginConfig.class);
            config.put(pluginConfig.getId(), pluginConfig);
            buildAdvice(pluginConfig);
            enablePlugin(pluginConfig.getId());
        }

        jsonReader.endArray();
        jsonReader.close();
    }

    public void doBefore() {
        //TODO 初始化所有的插件
        if (!init) {
            init = true;
            try {
                reloadConfigLocal();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("初始化插件失败");
            }
        }
    }
}
