package com.anoyi.grpc.service;

import com.anoyi.grpc.constant.GrpcResponseStatus;
import lombok.Data;

import java.io.Serializable;

@Data
public class GrpcResponse implements Serializable {

    private static final long serialVersionUID = -7161518426386434816L;

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

    public GrpcResponse error(String message, Throwable exception, StackTraceElement[] stackTrace){
        this.status = GrpcResponseStatus.ERROR.getCode();
        this.message = message;
        this.exception = exception;
        this.stackTrace = stackTrace;
        return this;
    }

    public GrpcResponse success(Object result){
        this.status = GrpcResponseStatus.SUCCESS.getCode();
        this.result = result;
        return this;
    }

}
