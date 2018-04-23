package org.springframework.grpc;

import com.alibaba.fastjson.JSON;
import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import io.grpc.ManagedChannel;
import org.springframework.grpc.service.GrpcRequest;
import org.springframework.grpc.service.GrpcResponse;

public class ServerContext {

    private ManagedChannel channel;

    private CommonServiceGrpc.CommonServiceBlockingStub blockingStub;

    ServerContext(ManagedChannel channel){
        this.channel = channel;
        blockingStub = CommonServiceGrpc.newBlockingStub(channel);
    }

    public GrpcResponse handle(GrpcRequest grpcRequest) throws Exception {
        GrpcService.Request request = GrpcService.Request.newBuilder().setRequest(JSON.toJSONString(grpcRequest)).build();
        String response = blockingStub.handle(request).getReponse();
        return JSON.parseObject(response, GrpcResponse.class);
    }

    public ManagedChannel getChannel() {
        return channel;
    }

}
