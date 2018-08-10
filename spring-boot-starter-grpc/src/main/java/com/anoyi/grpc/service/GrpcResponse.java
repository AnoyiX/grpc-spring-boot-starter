package com.anoyi.grpc.service;

import com.anoyi.grpc.constant.GrpcResponseStatus;

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

    /**
     * 异常堆栈信息
     */
    private StackTraceElement[] stackTrace;

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
        this.status = GrpcResponseStatus.ERROR.getCode();
        this.message = message;
    }

    public void success(Object result){
        this.status = GrpcResponseStatus.SUCCESS.getCode();
        this.result = result;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }
}
