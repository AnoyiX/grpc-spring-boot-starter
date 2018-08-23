package com.anoyi.grpc;

import com.alibaba.fastjson.JSONObject;
import com.anoyi.grpc.config.GrpcAutoConfiguration;
import com.anoyi.grpc.service.CodecService;
import com.anoyi.grpc.service.GrpcRequest;
import com.anoyi.grpc.service.GrpcResponse;
import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import com.google.protobuf.ByteString;
import io.grpc.Channel;

import java.util.logging.Logger;

public class ServerContext {

    private static final Logger log = Logger.getLogger(GrpcAutoConfiguration.class.getName());

    private Channel channel;

    private CodecService codecService;

    private CommonServiceGrpc.CommonServiceBlockingStub blockingStub;

    ServerContext(Channel channel, CodecService codecService) {
        this.channel = channel;
        this.codecService = codecService;
        blockingStub = CommonServiceGrpc.newBlockingStub(channel);
    }

    /**
     * 处理 gRPC 请求
     */
    public GrpcResponse handle(GrpcRequest grpcRequest) {
        ByteString bytes = codecService.serialize(grpcRequest);
        GrpcService.Request request = GrpcService.Request.newBuilder().setRequest(bytes).build();
        GrpcService.Response response;
        try{
            response = blockingStub.withWaitForReady().handle(request);
        }catch (Exception e){
            log.warning("GRPC handle error, re-handle: " + JSONObject.toJSONString(grpcRequest));
            response = blockingStub.withWaitForReady().handle(request);
        }
        return codecService.deserialize(response);
    }

    /**
     * 获取 Channel
     */
    public Channel getChannel() {
        return channel;
    }

}
