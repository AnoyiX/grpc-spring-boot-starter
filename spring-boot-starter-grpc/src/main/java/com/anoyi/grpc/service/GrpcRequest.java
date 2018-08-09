package com.anoyi.grpc.service;

public class GrpcRequest {

    /**
     * 接口
     */
    private String clazz;

    /**
     * 方法
     */
    private String method;

    /**
     * service 方法参数
     */
    private Object[] args;

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

}
