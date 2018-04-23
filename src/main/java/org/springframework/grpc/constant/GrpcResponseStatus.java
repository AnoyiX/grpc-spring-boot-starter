package org.springframework.grpc.constant;

public enum GrpcResponseStatus {

    SUCCESS(0), ERROR(-1);

    private int code;

    GrpcResponseStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
