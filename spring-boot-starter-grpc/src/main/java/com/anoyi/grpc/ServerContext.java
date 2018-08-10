package com.anoyi.grpc;

import com.anoyi.grpc.service.CodecService;
import com.anoyi.grpc.service.GrpcRequest;
import com.anoyi.grpc.service.GrpcResponse;
import com.anoyi.grpc.util.ProtobufUtils;
import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import org.springframework.util.SerializationUtils;

public class ServerContext {

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
//        byte[] bytes = ProtobufUtils.serialize(grpcRequest);
        ByteString bytes = codecService.serialize(grpcRequest);
        GrpcService.Request request = GrpcService.Request.newBuilder().setRequest(bytes).build();
        GrpcService.Response response = blockingStub.handle(request);
        return codecService.deserialize(response);
//        ByteString responseBody = response.getResponse();
//        return ProtobufUtils.deserialize(responseBody.toByteArray(), GrpcResponse.class);
    }

    /**
     * 获取 Channel
     */
    public Channel getChannel() {
        return channel;
    }

}
