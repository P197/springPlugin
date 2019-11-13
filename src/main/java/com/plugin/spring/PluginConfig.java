package com.plugin.spring;

import com.alibaba.fastjson.annotation.JSONField;
import com.convert.URLDeserializer;

import java.net.URL;

/**
 * @author 12130
 * @date 2019/11/12
 * @time 12:46
 */
public class PluginConfig {
    private String id;
    /**
     * 插件的名称
     */
    private String name;
    /**
     * 文件的url，为什么是url而不是路径，因为文件可能会来自远程仓库，也可能来自本地文件
     */
    @JSONField(deserializeUsing = URLDeserializer.class)
    private URL url;
    private boolean active;
    private String version;
    private String className;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
