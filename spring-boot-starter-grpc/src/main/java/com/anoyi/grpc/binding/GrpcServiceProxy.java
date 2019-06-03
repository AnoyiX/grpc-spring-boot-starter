package com.anoyi.grpc.binding;

import com.anoyi.grpc.GrpcClient;
import com.anoyi.grpc.annotation.GrpcService;
import com.anoyi.grpc.annotation.InvokeTimeout;
import com.anoyi.grpc.constant.GrpcResponseStatus;
import com.anoyi.grpc.constant.SerializeType;
import com.anoyi.grpc.exception.GrpcException;
import com.anoyi.grpc.service.GrpcRequest;
import com.anoyi.grpc.service.GrpcResponse;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

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
        boolean isAsyncCall = false;
        if ("toString".equals(methodName) && args.length == 0) {
            return className + "@" + invoker.hashCode();
        } else if ("hashCode".equals(methodName) && args.length == 0) {
            return invoker.hashCode();
        } else if ("equals".equals(methodName) && args.length == 1) {
            Object another = args[0];
            return proxy == another;
        }
        GrpcService annotation = grpcService.getAnnotation(GrpcService.class);
        InvokeTimeout timeoutAnnotation = method.getAnnotation(InvokeTimeout.class);
        if (method.getReturnType() == CompletableFuture.class) {
            isAsyncCall = true;
        }
        String server = annotation.server();
        GrpcRequest request = new GrpcRequest();
        request.setClazz(className);
        request.setMethod(methodName);
        request.setArgs(args);
        SerializeType[] serializeTypeArray = annotation.serialization();
        SerializeType serializeType = null;
        if (serializeTypeArray.length > 0) {
            serializeType = serializeTypeArray[0];
        }
        if (isAsyncCall) {
            CompletableFuture<GrpcResponse> sourceFuture = GrpcClient.connect(server).asyncHandle(serializeType,
                    request, timeoutAnnotation == null ? 0 : timeoutAnnotation.value(),
                    timeoutAnnotation == null ? null : timeoutAnnotation.timeUnit());
            CompletableFuture targetFuture = new CompletableFuture() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    super.cancel(mayInterruptIfRunning);
                    return sourceFuture.cancel(mayInterruptIfRunning);
                }
            };
            sourceFuture.whenComplete((response, throwable) -> {
                if (throwable != null) {
                    targetFuture.completeExceptionally(throwable);
                } else {
                    if (GrpcResponseStatus.ERROR.getCode() == response.getStatus()) {
                        targetFuture.completeExceptionally(parseThrowable(response));
                    } else {
                        targetFuture.complete(response.getResult());
                    }
                }
            });
            return targetFuture;
        } else {
            GrpcResponse response = GrpcClient.connect(server).handle(serializeType, request,
                    timeoutAnnotation == null ? 0 : timeoutAnnotation.value(),
                    timeoutAnnotation == null ? null : timeoutAnnotation.timeUnit());
            if (GrpcResponseStatus.ERROR.getCode() == response.getStatus()) {
                throw parseThrowable(response);
            }
            return response.getResult();
        }
    }

    private Throwable parseThrowable(GrpcResponse response) {
        Throwable throwable = response.getException();
        GrpcException exception = new GrpcException(throwable.getClass().getName() + ": " + throwable.getMessage());
        StackTraceElement[] exceptionStackTrace = exception.getStackTrace();
        StackTraceElement[] responseStackTrace = response.getStackTrace();
        StackTraceElement[] allStackTrace = Arrays.copyOf(exceptionStackTrace, exceptionStackTrace.length +
                responseStackTrace.length);
        System.arraycopy(responseStackTrace, 0, allStackTrace, exceptionStackTrace.length, responseStackTrace
                .length);
        exception.setStackTrace(allStackTrace);
        return throwable;
    }
}
