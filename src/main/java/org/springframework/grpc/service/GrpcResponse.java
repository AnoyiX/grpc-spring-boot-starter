package org.springframework.grpc.service;

public class GrpcResponse {

    /**
     * 响应状态
     */
    private int status;

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

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
