package com.anoyi.grpc.facade.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 宠物
 */
@Data
public class PetEntity implements Serializable {

    private static final long serialVersionUID = 0L;

    private String type;

    private String name;

    private UserEntity owner;

}
