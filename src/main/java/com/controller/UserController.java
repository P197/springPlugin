package com.controller;

import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 12130
 * @date 2019/11/12
 * @time 20:57
 * <p>
 * 测试的controller
 */
@Controller
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping("/test")
    public void userInfo() {
        userService.add();
    }
}
