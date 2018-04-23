package org.springframework.grpc.service;

import com.alibaba.fastjson.JSON;
import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import io.grpc.stub.StreamObserver;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.grpc.constant.GrpcResponseStatus;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class CommonService extends CommonServiceGrpc.CommonServiceImplBase {

    private static final Logger log = Logger.getLogger(CommonService.class.getName());

    private final AbstractApplicationContext applicationContext;

    public CommonService(AbstractApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void handle(GrpcService.Request request, StreamObserver<GrpcService.Response> responseObserver) {
        log.info("gRPC request: " + request);
        String req = request.getRequest();
        GrpcRequest grpcRequest = JSON.parseObject(req, GrpcRequest.class);
        Object bean = applicationContext.getBeanFactory().getBean(grpcRequest.getServiceBeanName());
        GrpcResponse response = new GrpcResponse();
        try {
            Object[] args = grpcRequest.getArgs();
            Object result;
            if (args != null) {
                Class[] argsClass = new Class[args.length];
                for (int i = 0, j = args.length; i < j; i++) {
                    argsClass[i] = args[i].getClass();
                }
                Method method = bean.getClass().getDeclaredMethod(grpcRequest.getServiceMethodName(), argsClass);
                result = method.invoke(bean, args);
            }else {
                Method method = bean.getClass().getDeclaredMethod(grpcRequest.getServiceMethodName());
                result = method.invoke(bean);
            }
            response.setStatus(GrpcResponseStatus.SUCCESS.getCode());
            response.setResult(result);
        } catch (Exception e) {
            log.warning(e.getMessage());
            e.printStackTrace();
            response.setStatus(GrpcResponseStatus.ERROR.getCode());
        }
        GrpcService.Response grpcResponse = GrpcService.Response.newBuilder().setReponse(JSON.toJSONString(response)).build();
        responseObserver.onNext(grpcResponse);
        responseObserver.onCompleted();
    }

}
