package org.springframework.grpc;

import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import org.springframework.grpc.service.GrpcRequest;
import org.springframework.grpc.service.GrpcResponse;
import org.springframework.grpc.util.ProtobufUtils;

public class ServerContext {

    private ManagedChannel channel;

    private CommonServiceGrpc.CommonServiceBlockingStub blockingStub;

    ServerContext(ManagedChannel channel){
        this.channel = channel;
        blockingStub = CommonServiceGrpc.newBlockingStub(channel);
    }

    public GrpcResponse handle(GrpcRequest grpcRequest) throws Exception {
        byte[] bytes = ProtobufUtils.serialize(grpcRequest);
        GrpcService.Request request = GrpcService.Request.newBuilder().setRequest(ByteString.copyFrom(bytes)).build();
        ByteString response = blockingStub.handle(request).getReponse();
        return ProtobufUtils.deserialize(response.toByteArray(), GrpcResponse.class);
    }

    public ManagedChannel getChannel() {
        return channel;
    }

}
