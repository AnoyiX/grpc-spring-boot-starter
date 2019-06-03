package com.anoyi.grpc;

import com.anoyi.grpc.constant.SerializeType;
import com.anoyi.grpc.service.GrpcRequest;
import com.anoyi.grpc.service.GrpcResponse;
import com.anoyi.grpc.service.SerializeService;
import com.anoyi.grpc.util.ProtobufUtils;
import com.anoyi.grpc.util.SerializeUtils;
import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.anoyi.grpc.config.GrpcProperties.DEFAULT_INVOKE_TIMEOUT;

@Slf4j
public class ServerContext {

    private Channel channel;

    private final SerializeService defaultSerializeService;

    private CommonServiceGrpc.CommonServiceBlockingStub blockingStub;
    private CommonServiceGrpc.CommonServiceFutureStub futureStub;
    private Executor executor;

    ServerContext(Channel channel, SerializeService serializeService,Executor executor) {
        this.channel = channel;
        this.defaultSerializeService = serializeService;
        this.blockingStub = CommonServiceGrpc.newBlockingStub(channel);
        this.futureStub = CommonServiceGrpc.newFutureStub(channel);
        this.executor = executor;
    }

    /**
     * 处理 gRPC 请求
     */
    public GrpcResponse handle(SerializeType serializeType, GrpcRequest grpcRequest, int timeout, TimeUnit
            timeoutUnit) {
        SerializeService serializeService = SerializeUtils.getSerializeService(serializeType, this
                .defaultSerializeService);
        ByteString bytes = serializeService.serialize(grpcRequest);
        int value = (serializeType == null ? -1 : serializeType.getValue());
        GrpcService.Request request = GrpcService.Request.newBuilder().setSerialize(value).setRequest(bytes).build();
        GrpcService.Response response = null;
        try {
            if (timeout <= 0) {
                response = blockingStub.withDeadlineAfter(DEFAULT_INVOKE_TIMEOUT, TimeUnit.SECONDS).handle(request);
            } else {
                response = blockingStub.withDeadlineAfter(timeout, timeoutUnit).handle(request);
            }
        } catch (Exception exception) {
            log.warn("rpc exception: {}", exception.getMessage());
            if ("UNAVAILABLE: io exception".equals(exception.getMessage().trim())) {
                response = blockingStub.withWaitForReady().handle(request);
            }
        }
        return serializeService.deserialize(response);
    }

    public CompletableFuture<GrpcResponse> asyncHandle(final SerializeType serializeType, final GrpcRequest grpcRequest,
                                                       final int timeout, final TimeUnit timeoutUnit) {
        SerializeService serializeService = SerializeUtils.getSerializeService(serializeType, this
                .defaultSerializeService);
        ByteString bytes = serializeService.serialize(grpcRequest);
        int value = (serializeType == null ? -1 : serializeType.getValue());
        GrpcService.Request request = GrpcService.Request.newBuilder().setSerialize(value).setRequest(bytes).build();
        ListenableFuture<GrpcService.Response> sourceFuture;
        if (timeout <= 0) {
            sourceFuture = futureStub.withDeadlineAfter(
                    DEFAULT_INVOKE_TIMEOUT, TimeUnit.SECONDS).handle(request);
        } else {
            sourceFuture = futureStub.withDeadlineAfter(
                    timeout, timeoutUnit).handle(request);
        }

        CompletableFuture<GrpcResponse> targetFuture = new CompletableFuture<GrpcResponse>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                super.cancel(mayInterruptIfRunning);
                return sourceFuture.cancel(mayInterruptIfRunning);
            }
        };

        sourceFuture.addListener(() -> {
            GrpcResponse grpcResponse = null;
            try {
                grpcResponse = serializeService.deserialize(sourceFuture.get());
                targetFuture.complete(grpcResponse);
            } catch (Exception e) {
                log.warn("rpc exception: {}", e.getMessage());
                targetFuture.completeExceptionally(e.getCause());
            }
        }, executor);
        return targetFuture;
    }

    /**
     * 获取 Channel
     */
    public Channel getChannel() {
        return channel;
    }

}
