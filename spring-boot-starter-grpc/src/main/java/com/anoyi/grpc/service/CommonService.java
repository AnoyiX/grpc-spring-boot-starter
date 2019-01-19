package com.anoyi.grpc.service;

import com.anoyi.grpc.util.SerializeUtils;
import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.support.AbstractApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CommonService extends CommonServiceGrpc.CommonServiceImplBase {

    private Map<Class, Object> serviceBeanMap = new ConcurrentHashMap<>();

    private final AbstractApplicationContext applicationContext;

    private final SerializeService defaultSerializationService;

    public CommonService(AbstractApplicationContext applicationContext, SerializeService serializeService) {
        this.applicationContext = applicationContext;
        this.defaultSerializationService = serializeService;
    }

    @Override
    public void handle(GrpcService.Request request, StreamObserver<GrpcService.Response> responseObserver) {
        int serialize = request.getSerialize();
        SerializeService serializeService = SerializeUtils.getSerializeService(serialize, defaultSerializationService);
        GrpcRequest grpcRequest = serializeService.deserialize(request);
        GrpcResponse response = new GrpcResponse();
        try {
            String className = grpcRequest.getClazz();
            Object bean = getBean(Class.forName(className));
            Object[] args = grpcRequest.getArgs();
            Class[] argsTypes = getParameterTypes(args);
            Method matchingMethod = MethodUtils.getMatchingMethod(Class.forName(className), grpcRequest.getMethod(), argsTypes);
            FastClass serviceFastClass = FastClass.create(bean.getClass());
            FastMethod serviceFastMethod = serviceFastClass.getMethod(matchingMethod);
            Object result = serviceFastMethod.invoke(bean, args);
            response.success(result);
        } catch (NoSuchBeanDefinitionException | ClassNotFoundException exception) {
            String message = exception.getClass().getName() + ": " + exception.getMessage();
            response.error(message, exception, exception.getStackTrace());
            log.error("method not implement", exception.getCause());
        } catch (InvocationTargetException exception) {
            String message = exception.getCause().getClass().getName() + ": " + exception.getCause().getMessage();
            response.error(message, exception.getCause(), exception.getCause().getStackTrace());
            log.error("method invoke error", exception.getCause());
        }
        ByteString bytes = serializeService.serialize(response);
        GrpcService.Response grpcResponse = GrpcService.Response.newBuilder().setResponse(bytes).build();
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

    /**
     * 获取参数类型
     */
    private Class[] getParameterTypes(Object[] parameters){
        if (parameters == null){
            return null;
        }
        Class[] clazzArray = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            clazzArray[i] = parameters[i].getClass();

        }
        return clazzArray;
    }

}
