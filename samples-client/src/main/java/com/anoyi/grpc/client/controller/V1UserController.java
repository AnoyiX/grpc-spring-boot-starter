package com.anoyi.grpc.client.controller;

import com.alibaba.fastjson.JSONObject;
import com.anoyi.grpc.facade.entity.UserEntity;
import com.anoyi.grpc.facade.service.UserServiceBySofaHessian;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/user")
public class V1UserController {

    private final UserServiceBySofaHessian userServiceBySofaHessian;

    @PostMapping("/add")
    public UserEntity insertUser(@RequestBody UserEntity userEntity) {
        userServiceBySofaHessian.insert(userEntity);
        return userEntity;
    }

    @GetMapping("/list")
    public List<UserEntity> findAllUser() {
        return userServiceBySofaHessian.findAll();
    }

    @PostMapping("/remove")
    public String removeUser(@RequestParam("id") Long id) {
        userServiceBySofaHessian.deleteById(id);
        return "success";
    }

    /**
     * 模拟异步请求
     *
     * @return
     */
    @GetMapping("/listAsync")
    public DeferredResult<List<UserEntity>> findAllUserAsync() {
        DeferredResult<List<UserEntity>> result = new DeferredResult<>(60 * 1000L);
        userServiceBySofaHessian.findAllAsync().whenComplete((r, t) -> {
            if (t != null) {
                result.setErrorResult(new JSONObject().put("error", t.getMessage()));
            }
            result.setResult(r);
        });
        return result;
    }

}
