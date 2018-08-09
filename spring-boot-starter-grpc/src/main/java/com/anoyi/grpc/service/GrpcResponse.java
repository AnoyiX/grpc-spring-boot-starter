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
     * 服务端异常
     */
    private Throwable exception;

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

    public void error(Throwable exception){
        this.status = GrpcResponseStatus.ERROR.getCode();
        this.exception = exception;
        this.stackTrace = exception.getStackTrace();
    }

    public void success(Object result){
        this.status = GrpcResponseStatus.SUCCESS.getCode();
        this.result = result;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }
}
