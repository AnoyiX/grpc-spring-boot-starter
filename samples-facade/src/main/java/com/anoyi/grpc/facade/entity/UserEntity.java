package com.anoyi.grpc.facade.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 人物
 */
@Data
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 0L;

    private Long id;

    private String name;

    private int age;

    private String gender;

    private Map<String, Integer> scores;

    private UserEntity friend;

    private PetEntity pet;

    private List<Object> listValue;

}
