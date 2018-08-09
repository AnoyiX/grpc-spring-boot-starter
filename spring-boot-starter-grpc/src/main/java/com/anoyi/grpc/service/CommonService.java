package com.anoyi.grpc.service;

import com.anoyi.grpc.util.ProtobufUtils;
import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.support.AbstractApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommonService extends CommonServiceGrpc.CommonServiceImplBase {

    private Map<Class, Object> serviceBeanMap = new ConcurrentHashMap<>();

    private final AbstractApplicationContext applicationContext;

    public CommonService(AbstractApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void handle(GrpcService.Request request, StreamObserver<GrpcService.Response> responseObserver) {
        GrpcRequest grpcRequest = ProtobufUtils.deserialize(request.getRequest().toByteArray(), GrpcRequest.class);
        GrpcResponse response = new GrpcResponse();
        try {
            Object bean = getBean(grpcRequest.getClazz());
            Object[] args = grpcRequest.getArgs();
            FastClass serviceFastClass = FastClass.create(bean.getClass());
            FastMethod serviceFastMethod = serviceFastClass.getMethod(grpcRequest.getMethod());
            Object result = serviceFastMethod.invoke(bean, args);
            response.success(result);
        } catch (NoSuchBeanDefinitionException noSuchBeanDefinitionException) {
            response.error(noSuchBeanDefinitionException);
        } catch (InvocationTargetException invocationTargetException) {
            response.error(invocationTargetException.getTargetException());
        }
        ByteString bytes = ByteString.copyFrom(ProtobufUtils.serialize(response));
        GrpcService.Response grpcResponse = GrpcService.Response.newBuilder().setReponse(bytes).build();
        responseObserver.onNext(grpcResponse);
        responseObserver.onCompleted();
    }

    /**
     * 获取 Service Bean
     */
    private Object getBean(Class clazz) throws NoSuchBeanDefinitionException {
        if (serviceBeanMap.containsKey(clazz)) {
            return serviceBeanMap.get(clazz);
        }
        try {
            Object bean = applicationContext.getBean(clazz);
            serviceBeanMap.put(clazz, bean);
            return bean;
        } catch (BeansException e) {
            throw new NoSuchBeanDefinitionException(clazz);
        }
    }

}
