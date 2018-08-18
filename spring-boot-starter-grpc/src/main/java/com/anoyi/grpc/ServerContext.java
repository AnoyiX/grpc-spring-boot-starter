package com.anoyi.grpc;

import com.anoyi.grpc.service.GrpcRequest;
import com.anoyi.grpc.service.GrpcResponse;
import com.anoyi.grpc.util.ProtobufUtils;
import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import com.google.protobuf.ByteString;
import io.grpc.Channel;

public class ServerContext {

    private Channel channel;

    private CommonServiceGrpc.CommonServiceBlockingStub blockingStub;

    ServerContext(Channel channel) {
        this.channel = channel;
        blockingStub = CommonServiceGrpc.newBlockingStub(channel);
    }

    /**
     * 处理 gRPC 请求
     */
    public GrpcResponse handle(GrpcRequest grpcRequest) {
        byte[] bytes = ProtobufUtils.serialize(grpcRequest);
        GrpcService.Request request = GrpcService.Request.newBuilder().setRequest(ByteString.copyFrom(bytes)).build();
        GrpcService.Response response = blockingStub.handle(request);
        ByteString responseBody = response.getResponse();
        return ProtobufUtils.deserialize(responseBody.toByteArray(), GrpcResponse.class);
    }

    /**
     * 获取 Channel
     */
    public Channel getChannel() {
        return channel;
    }

}
