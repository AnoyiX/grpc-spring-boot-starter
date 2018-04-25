package org.springframework.grpc.service;

import org.springframework.grpc.constant.GrpcResponseStatus;

public class GrpcResponse {

    /**
     * 响应状态
     */
    private int status;

    /**
     * 信息提示
     */
    private String message;

    /**
     * 返回结果
     */
    private Object result;


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void error(String message){
        this.message = message;
        this.status = GrpcResponseStatus.ERROR.getCode();
    }

    public void success(Object result){
        this.result = result;
        this.status = GrpcResponseStatus.SUCCESS.getCode();
    }

}
