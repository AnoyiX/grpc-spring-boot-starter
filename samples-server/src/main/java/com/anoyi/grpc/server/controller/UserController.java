package com.anoyi.grpc.server.controller;

import com.anoyi.grpc.facade.service.UserServiceBySofaHessian;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController {

    private final UserServiceBySofaHessian userServiceBySofaHessian;

    @RequestMapping("/")
    public String hello(){
        return userServiceBySofaHessian.findAll().toString();
    }

}
