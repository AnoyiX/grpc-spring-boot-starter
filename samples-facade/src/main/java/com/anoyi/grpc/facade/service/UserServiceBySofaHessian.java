package com.anoyi.grpc.facade.service;


import com.anoyi.grpc.annotation.GrpcService;
import com.anoyi.grpc.annotation.InvokeTimeout;
import com.anoyi.grpc.facade.entity.UserEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@GrpcService(server = "user")
public interface UserServiceBySofaHessian {

    void insert(UserEntity userEntity);

    void deleteById(Long id);

    @InvokeTimeout(2)
    List<UserEntity> findAll();

    /**
     * 异步调用
     * @return
     */
    @InvokeTimeout(5)
    CompletableFuture<List<UserEntity>> findAllAsync();
}
