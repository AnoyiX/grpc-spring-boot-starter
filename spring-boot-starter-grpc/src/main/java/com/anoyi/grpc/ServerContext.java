package com.anoyi.grpc;

import com.anoyi.grpc.config.GrpcProperties;
import com.anoyi.grpc.constant.SerializeType;
import com.anoyi.grpc.service.GrpcRequest;
import com.anoyi.grpc.service.GrpcResponse;
import com.anoyi.grpc.service.SerializeService;
import com.anoyi.grpc.util.SerializeUtils;
import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ServerContext {

    private Channel channel;

    private final SerializeService defaultSerializeService;

    private final GrpcProperties grpcProperties;

    private CommonServiceGrpc.CommonServiceBlockingStub blockingStub;

    ServerContext(Channel channel, SerializeService serializeService, GrpcProperties grpcProperties) {
        this.channel = channel;
        this.defaultSerializeService = serializeService;
        this.grpcProperties = grpcProperties;
        blockingStub = CommonServiceGrpc.newBlockingStub(channel);
    }

    /**
     * 处理 gRPC 请求
     */
    public GrpcResponse handle(SerializeType serializeType, GrpcRequest grpcRequest, com.anoyi.grpc.annotation.GrpcService annotation) {
        SerializeService serializeService = SerializeUtils.getSerializeService(serializeType, this.defaultSerializeService);
        ByteString bytes = serializeService.serialize(grpcRequest);
        int value = (serializeType == null ? -1 : serializeType.getValue());
        GrpcService.Request request = GrpcService.Request.newBuilder().setSerialize(value).setRequest(bytes).build();
        GrpcService.Response response = null;
        //设置自定义请求头
        Metadata metadata = new Metadata();
        if(StringUtils.hasText(grpcProperties.getNginxHost())){
            metadata.put(Metadata.Key.of("grpc-server", Metadata.ASCII_STRING_MARSHALLER), annotation.server());
            metadata.put(Metadata.Key.of("grpc-clazz", Metadata.ASCII_STRING_MARSHALLER), grpcRequest.getClazz());
        }
        try{
            response = MetadataUtils.attachHeaders(blockingStub.withDeadlineAfter(grpcProperties.getDuration(), TimeUnit.MILLISECONDS), metadata).handle(request);
        }catch (Exception exception){
            log.warn("rpc exception: {}", exception.getMessage());
            if ("UNAVAILABLE: io exception".equals(exception.getMessage().trim())){
                response = MetadataUtils.attachHeaders(blockingStub.withDeadlineAfter(grpcProperties.getDuration(), TimeUnit.MILLISECONDS), metadata).handle(request);
            }
        }
        return serializeService.deserialize(response);
    }

    /**
     * 获取 Channel
     */
    public Channel getChannel() {
        return channel;
    }

}
