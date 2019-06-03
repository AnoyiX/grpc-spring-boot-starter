package com.anoyi.grpc.facade.service;


import com.anoyi.grpc.annotation.GrpcService;
import com.anoyi.grpc.annotation.InvokeTimeout;
import com.anoyi.grpc.constant.SerializeType;
import com.anoyi.grpc.facade.entity.UserEntity;

import java.util.List;
import java.util.concurrent.TimeUnit;

@GrpcService(server = "user", serialization = SerializeType.FASTJSON)
public interface UserServiceByFastJSON {

    void insert(String userEntityJson);

    void deleteById(String id);

    @InvokeTimeout(value = 2)
    List<UserEntity> findAll();

}
