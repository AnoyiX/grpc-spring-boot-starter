package com.anoyi.grpc.service;

public class GrpcRequest {

    /**
     * service 类名
     */
    private String beanName;

    /**
     * service 方法名
     */
    private String methodName;

    /**
     * service 方法参数
     */
    private Object[] args;

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

}
