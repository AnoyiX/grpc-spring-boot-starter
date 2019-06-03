package com.anoyi.grpc;

import com.anoyi.grpc.config.GrpcProperties;
import com.anoyi.grpc.config.RemoteServer;
import com.anoyi.grpc.service.SerializeService;
import io.grpc.*;
import io.grpc.internal.DnsNameResolverProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GrpcClient {

    private static final Map<String, ServerContext> serverMap = new HashMap<>();

    private final GrpcProperties grpcProperties;

    private final SerializeService serializeService;

    private ClientInterceptor clientInterceptor;

    private Executor executor;

    public GrpcClient(GrpcProperties grpcProperties, SerializeService serializeService, Executor executor) {
        this(grpcProperties, serializeService, null, executor);
    }

    public GrpcClient(GrpcProperties grpcProperties, SerializeService serializeService, ClientInterceptor
            clientInterceptor, Executor executor) {
        this.grpcProperties = grpcProperties;
        this.serializeService = serializeService;
        this.clientInterceptor = clientInterceptor;
        this.executor = executor;
    }

    /**
     * 初始化
     */
    public void init() {
        List<RemoteServer> remoteServers = grpcProperties.getRemoteServers();
        if (!CollectionUtils.isEmpty(remoteServers)) {
            for (RemoteServer server : remoteServers) {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(server.getHost(), server.getPort())
                        .defaultLoadBalancingPolicy("round_robin")
                        .nameResolverFactory(DnsNameResolverProvider.asFactory())
                        .idleTimeout(30, TimeUnit.SECONDS)
                        .usePlaintext().build();
                if (clientInterceptor != null) {
                    Channel newChannel = ClientInterceptors.intercept(channel, clientInterceptor);
                    serverMap.put(server.getServer(), new ServerContext(newChannel, serializeService, executor));
                } else {
                    Class clazz = grpcProperties.getClientInterceptor();
                    if (clazz == null) {
                        serverMap.put(server.getServer(), new ServerContext(channel, serializeService, executor));
                    } else {
                        try {
                            ClientInterceptor interceptor = (ClientInterceptor) clazz.newInstance();
                            Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
                            serverMap.put(server.getServer(), new ServerContext(newChannel, serializeService,
                                    executor));
                        } catch (InstantiationException | IllegalAccessException e) {
                            log.warn("ClientInterceptor cannot use, ignoring...");
                            serverMap.put(server.getServer(), new ServerContext(channel, serializeService, executor));
                        }
                    }
                }
            }
        }
    }

    /**
     * 连接远程服务
     */
    public static ServerContext connect(String serverName) {
        return serverMap.get(serverName);
    }

}
