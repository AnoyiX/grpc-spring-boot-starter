package com.anoyi.grpc.facade.service;


import com.anoyi.grpc.annotation.GrpcService;
import com.anoyi.grpc.facade.entity.UserEntity;

import java.util.List;

@GrpcService(server = "user")
public interface UserService {

    void insert(UserEntity userEntity);

    void deleteById(Long id);

    void update(UserEntity userEntity);

    UserEntity findById(Long id);

    List<UserEntity> findAll();

    void insertAll(List<UserEntity> userEntityList);

}
