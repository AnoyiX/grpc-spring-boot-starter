package com.anoyi.grpc.binding;


import com.anoyi.grpc.service.GrpcRequest;
import org.springframework.cglib.proxy.InvocationHandler;
import com.anoyi.grpc.GrpcClient;
import com.anoyi.grpc.annotation.GrpcService;
import org.springframework.util.StringUtils;

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
        String beanName = annotation.bean();
        if (StringUtils.isEmpty(beanName)){
            beanName = Character.toLowerCase(className.charAt(0)) + className.substring(1);
        }
        request.setBeanName(beanName);
        request.setMethodName(method.getName());
        request.setArgs(args);
        return GrpcClient.connect(server).handle(request).getResult();
    }

}
