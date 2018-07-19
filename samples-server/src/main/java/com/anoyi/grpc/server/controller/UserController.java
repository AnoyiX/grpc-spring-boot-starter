package com.anoyi.grpc.server.controller;

import com.anoyi.grpc.facade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public String hello(){
        return userService.findAll().toString();
    }

}
