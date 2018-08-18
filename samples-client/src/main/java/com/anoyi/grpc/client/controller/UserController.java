package com.anoyi.grpc.client.controller;

import com.anoyi.grpc.facade.entity.UserEntity;
import com.anoyi.grpc.facade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public UserEntity insertUser(@RequestBody UserEntity userEntity){
        userService.insert(userEntity);
        return userEntity;
    }

    @PostMapping("/addAll")
    public List<UserEntity> insertUsers(@RequestBody List<UserEntity> userEntityList){
        userService.insertAll(userEntityList);
        return userService.findAll();
    }

    @GetMapping("/find")
    public UserEntity findUser(@RequestParam("id") Long id){
        return userService.findById(id);
    }

    @PostMapping("/update")
    public UserEntity updateUser(@RequestBody UserEntity userEntity){
        userService.update(userEntity);
        return userEntity;
    }

    @GetMapping("/list")
    public List<UserEntity> findAllUser(){
        return userService.findAll();
    }

    @PostMapping("/remove")
    public UserEntity removeUser(@RequestParam("id") Long id){
        UserEntity userEntity = userService.findById(id);
        userService.deleteById(id);
        return userEntity;
    }

}
