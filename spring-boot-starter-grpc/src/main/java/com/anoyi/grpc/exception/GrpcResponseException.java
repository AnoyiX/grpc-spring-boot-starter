package com.anoyi.grpc.exception;

import com.anoyi.grpc.service.GrpcResponse;

public class GrpcResponseException extends RuntimeException {

    private GrpcResponse response;

    public GrpcResponseException(GrpcResponse response){
        super(response.getMessage());
        this.response = response;
    }


    public GrpcResponse getResponse() {
        return response;
    }

    public void setResponse(GrpcResponse response) {
        this.response = response;
    }
}
