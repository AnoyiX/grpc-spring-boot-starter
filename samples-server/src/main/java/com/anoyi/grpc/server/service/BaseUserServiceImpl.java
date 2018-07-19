package com.anoyi.grpc.server.service;

import com.anoyi.grpc.facade.entity.UserEntity;
import com.anoyi.grpc.facade.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class BaseUserServiceImpl implements UserService {

    /**
     * 模拟数据库存储用户信息
     */
    private Map<Long, UserEntity> userMap = new ConcurrentHashMap<>();

    @Override
    public void insert(UserEntity userEntity) {
        if (userEntity == null){
            log.warn("insert user fail, userEntity is null!");
            return ;
        }
        userMap.putIfAbsent(userEntity.getId(), userEntity);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null){
            log.warn("delete user fail, id is null!");
        }
        userMap.remove(id);
    }

    @Override
    public void update(UserEntity userEntity) {
        if (userEntity == null){
            log.warn("update user fail, userEntity is empty!");
            return ;
        }
        userMap.put(userEntity.getId(), userEntity);
    }

    @Override
    public UserEntity findById(Long id) {
        if (id == null){
            log.warn("find user fail, id is null!");
        }
        return userMap.get(id);
    }

    @Override
    public List<UserEntity> findAll() {
        log.info("base find all users");
        Collection<UserEntity> values = userMap.values();
        if (values.isEmpty()){
            UserEntity userEntity = new UserEntity();
            userEntity.setId(0L);
            return (new ArrayList<>(Arrays.asList(userEntity)));
        }
        return new ArrayList<>(values);
    }

}
