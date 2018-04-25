package org.springframework.grpc.service;

import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.grpc.constant.GrpcResponseStatus;
import org.springframework.grpc.util.ProtobufUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
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
            try {
                Object[] args = grpcRequest.getArgs();
                Object result;
                if (args != null) {
                    Class[] argsClass = new Class[args.length];
                    for (int i = 0, j = args.length; i < j; i++) {
                        argsClass[i] = args[i].getClass();
                    }
                    Method method = bean.getClass().getDeclaredMethod(grpcRequest.getMethodName(), argsClass);
                    result = method.invoke(bean, args);
                } else {
                    Method method = bean.getClass().getDeclaredMethod(grpcRequest.getMethodName());
                    result = method.invoke(bean);
                }
                response.setStatus(GrpcResponseStatus.SUCCESS.getCode());
                response.setResult(result);
            } catch (Exception e) {
                log.warning(e.getMessage());
                e.printStackTrace();
                response.setStatus(GrpcResponseStatus.ERROR.getCode());
            }
        } else {
            response.setMessage("Service bean '" + beanName + "' not found.");
            response.setStatus(GrpcResponseStatus.ERROR.getCode());
        }
        ByteString bytes = ByteString.copyFrom(ProtobufUtils.serialize(response));
        GrpcService.Response grpcResponse = GrpcService.Response.newBuilder().setReponse(bytes).build();
        responseObserver.onNext(grpcResponse);
        responseObserver.onCompleted();
    }

    /**
     * Get Service Bean
     */
    private Object getBean(String beanName) throws NoSuchBeanDefinitionException{
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
                if (serviceBeanName.contains(beanName)){
                    Object bean = applicationContext.getBean(serviceBeanName);
                    serviceBeanMap.put(beanName, bean);
                    return bean;
                }
            }
        }
        return null;
    }

}
