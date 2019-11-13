package com.controller;

import com.plugin.spring.PluginConfig;
import com.plugin.spring.SpringPluginFactory;
import com.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 12130
 * @date 2019/11/12
 * @time 16:19
 * <p>
 * 插件控制器，通过该控制器控制插件的安装激活删除卸载。
 */
@RestController
@RequestMapping("/plugin")
public class SpringPluginManagerController {

    @Autowired
    SpringPluginFactory springPluginFactory;

    @RequestMapping("/install")
    public ResponseVo install(@RequestBody PluginConfig pluginConfig) {
        if (pluginConfig.getUrl() == null) {
            return new ResponseVo(501, "URL异常");
        }
        try {
            springPluginFactory.installPlugin(pluginConfig);
        } catch (Exception e) {
            return new ResponseVo(500, "插件安装异常");
        }
        return new ResponseVo(200, "插件安装成功");

    }

    @RequestMapping("/enable")
    public void enable(@RequestParam("id") String pluginId) {
        springPluginFactory.enablePlugin(pluginId);
    }

    @RequestMapping("/disable")
    public void disable(@RequestParam("id") String pluginId) {
        springPluginFactory.disablePlugin(pluginId);
    }

    @RequestMapping("/remove")
    public void remove(@RequestParam("id") String pluginId) {
        springPluginFactory.enablePlugin(pluginId);
    }


}
