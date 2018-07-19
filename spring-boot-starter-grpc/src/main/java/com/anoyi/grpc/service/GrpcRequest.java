package com.anoyi.grpc.service;

import java.lang.reflect.Method;

public class GrpcRequest {

    /**
     * 接口
     */
    private Class clazz;

    /**
     * 方法
     */
    private Method method;

    /**
     * service 方法参数
     */
    private Object[] args;

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

}
