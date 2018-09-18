package com.anoyi.grpc.binding;

import com.anoyi.grpc.GrpcClient;
import com.anoyi.grpc.annotation.GrpcService;
import com.anoyi.grpc.constant.GrpcResponseStatus;
import com.anoyi.grpc.constant.SerializeType;
import com.anoyi.grpc.exception.GrpcException;
import com.anoyi.grpc.service.GrpcRequest;
import com.anoyi.grpc.service.GrpcResponse;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

public class GrpcServiceProxy<T> implements InvocationHandler {

    private Class<T> grpcService;

    private Object invoker;

    public GrpcServiceProxy(Class<T> grpcService, Object invoker) {
        this.grpcService = grpcService;
        this.invoker = invoker;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        String className = grpcService.getName();
        if ("toString".equals(methodName) && args.length == 0) {
            return className + "@" + invoker.hashCode();
        } else if ("hashCode".equals(methodName) && args.length == 0) {
            return invoker.hashCode();
        } else if ("equals".equals(methodName) && args.length == 1) {
            Object another = args[0];
            return proxy == another;
        }
        GrpcService annotation = grpcService.getAnnotation(GrpcService.class);
        String server = annotation.server();
        GrpcRequest request = new GrpcRequest();
        request.setClazz(className);
        request.setMethod(methodName);
        request.setArgs(args);
        SerializeType[] serializeTypeArray = annotation.serialization();
        SerializeType serializeType = null;
        if (serializeTypeArray.length > 0){
            serializeType = serializeTypeArray[0];
        }
        GrpcResponse response = GrpcClient.connect(server).handle(serializeType, request);
        if (GrpcResponseStatus.ERROR.getCode() == response.getStatus()) {
            Throwable throwable = response.getException();
            GrpcException exception = new GrpcException(throwable.getClass().getName() + ": " + throwable.getMessage());
            exception.setStackTrace(response.getStackTrace());
            throw exception;
        }
        return response.getResult();
    }

}
