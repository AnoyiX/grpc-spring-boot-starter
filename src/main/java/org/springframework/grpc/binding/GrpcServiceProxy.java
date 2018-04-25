package org.springframework.grpc.binding;


import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.grpc.GrpcClient;
import org.springframework.grpc.annotation.GrpcService;
import org.springframework.grpc.service.GrpcRequest;

import java.lang.reflect.Method;

public class GrpcServiceProxy<T> implements InvocationHandler {

    private Class<T> grpcService;

    public GrpcServiceProxy(Class<T> grpcService){
        this.grpcService = grpcService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        GrpcService annotation = grpcService.getAnnotation(GrpcService.class);
        String server = annotation.server();
        GrpcRequest request = new GrpcRequest();
        String className = grpcService.getSimpleName();
        request.setBeanName((new StringBuilder()).append(Character.toLowerCase(className.charAt(0))).append(className.substring(1)).toString());
        request.setMethodName(method.getName());
        request.setArgs(args);
        return GrpcClient.connect(server).handle(request).getResult();
    }

}
