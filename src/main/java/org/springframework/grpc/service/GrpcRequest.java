package org.springframework.grpc.service;

public class GrpcRequest {

    /**
     * service 类名
     */
    private String serviceBeanName;

    /**
     * service 方法名
     */
    private String serviceMethodName;

    /**
     * service 方法参数
     */
    private Object[] args;

    public String getServiceBeanName() {
        return serviceBeanName;
    }

    public String getServiceMethodName() {
        return serviceMethodName;
    }

    public void setServiceMethodName(String serviceMethodName) {
        this.serviceMethodName = serviceMethodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public void setServiceBeanName(String serviceBeanName) {
        this.serviceBeanName = serviceBeanName;
    }
}
