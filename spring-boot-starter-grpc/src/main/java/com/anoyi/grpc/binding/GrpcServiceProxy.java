package com.anoyi.grpc.binding;


import com.anoyi.grpc.constant.GrpcResponseStatus;
import com.anoyi.grpc.service.GrpcRequest;
import com.anoyi.grpc.service.GrpcResponse;
import org.springframework.cglib.proxy.InvocationHandler;
import com.anoyi.grpc.GrpcClient;
import com.anoyi.grpc.annotation.GrpcService;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

public class GrpcServiceProxy<T> implements InvocationHandler {

    private Class<T> grpcService;

    public GrpcServiceProxy(Class<T> grpcService) {
        this.grpcService = grpcService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        GrpcService annotation = grpcService.getAnnotation(GrpcService.class);
        String server = annotation.server();
        GrpcRequest request = new GrpcRequest();
        request.setClazz(grpcService);
        request.setMethod(method);
        request.setArgs(args);
        GrpcResponse response = GrpcClient.connect(server).handle(request);
        if (GrpcResponseStatus.ERROR.getCode() == response.getStatus()) {
            Throwable exception = response.getException();
            exception.setStackTrace(response.getStackTrace());
            throw exception;
        }
        return response.getResult();
    }

}
