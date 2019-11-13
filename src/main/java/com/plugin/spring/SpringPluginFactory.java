package com.plugin.spring;

/**
 * @author 12130
 * @date 2019/11/12
 * @time 12:45
 * <p>
 * <p>
 * spring插件工厂
 */
public interface SpringPluginFactory {
    /**
     * 安装插件
     *
     * @param config    插件信息
     */
    void installPlugin(PluginConfig config);

    /**
     * 启用插件
     *
     * @param pluginId  插件id
     */
    void enablePlugin(String pluginId);

    /**
     * 禁用插件
     *
     * @param pluginId  插件id
     */
    void disablePlugin(String pluginId);

    /**
     * 删除插件
     *
     * @param pluginId  插件id
     */
    void removePlugin(String pluginId);

}
