package com.anoyi.grpc.service;

import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.support.AbstractApplicationContext;
import com.anoyi.grpc.util.ProtobufUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class CommonService extends CommonServiceGrpc.CommonServiceImplBase {

    private static final Logger log = Logger.getLogger(CommonService.class.getName());

    private Map<String, Object> serviceBeanMap = new ConcurrentHashMap<>();

    private final AbstractApplicationContext applicationContext;

    public CommonService(AbstractApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void handle(GrpcService.Request request, StreamObserver<GrpcService.Response> responseObserver) {
        GrpcRequest grpcRequest = ProtobufUtils.deserialize(request.getRequest().toByteArray(), GrpcRequest.class);
        GrpcResponse response = new GrpcResponse();
        String beanName = grpcRequest.getBeanName();
        Object bean = getBean(beanName);
        if (bean != null) {
            String methodName = grpcRequest.getMethodName();
            Object[] args = grpcRequest.getArgs();
            FastClass serviceFastClass = FastClass.create(bean.getClass());
            Class<?>[] argTypes = getMethodParameterTypes(args);
            FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, argTypes);
            try {
                Object result = serviceFastMethod.invoke(bean, args);
                response.success(result);
            } catch (InvocationTargetException e) {
                response.error("InvocationTargetException : " + e.getMessage());
            }
        } else {
            response.error("Service bean '" + beanName + "' not found.");
        }
        ByteString bytes = ByteString.copyFrom(ProtobufUtils.serialize(response));
        GrpcService.Response grpcResponse = GrpcService.Response.newBuilder().setReponse(bytes).build();
        responseObserver.onNext(grpcResponse);
        responseObserver.onCompleted();
    }

    /**
     * Get service bean
     */
    private Object getBean(String beanName) throws NoSuchBeanDefinitionException {
        if (serviceBeanMap.containsKey(beanName)) {
            return serviceBeanMap.get(beanName);
        }
        try {
            Object bean = applicationContext.getBean(beanName);
            serviceBeanMap.put(beanName, bean);
            return bean;
        } catch (NoSuchBeanDefinitionException e) {
            // match bean
            String[] serviceBeanNames = applicationContext.getBeanNamesForAnnotation(Service.class);
            for (String serviceBeanName : serviceBeanNames) {
                if (serviceBeanName.contains(beanName)) {
                    Object bean = applicationContext.getBean(serviceBeanName);
                    serviceBeanMap.put(beanName, bean);
                    return bean;
                }
            }
        }
        return null;
    }

    /**
     * Get service method parameterTypes
     */
    private Class<?>[] getMethodParameterTypes(Object[] args){
        if (args != null){
            Class<?>[] types = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                Class<?> type = args[i].getClass();
                types[i] = type;
            }
            return types;
        }
        return null;
    }

}
