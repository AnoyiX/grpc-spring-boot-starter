package org.springframework.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.grpc.config.GrpcProperties;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrpcClient {

    private static final Map<String, ServerContext> serverMap = new HashMap<>();

    public GrpcClient(GrpcProperties grpcProperties) {
        List<RemoteServer> remoteServers = grpcProperties.getRemoteServers();
        if (!CollectionUtils.isEmpty(remoteServers)){
            for (RemoteServer server : remoteServers) {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(server.getHost(), server.getPort()).usePlaintext().build();
                serverMap.put(server.getServer(), new ServerContext(channel));
            }
        }
    }

    /**
     * 连接远程服务
     * @param host 远程服务的 hostname
     * @return gRPC channel
     */
    public static ServerContext connect(String host){
        return serverMap.get(host);
    }

}
