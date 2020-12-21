package com.anoyi.grpc;

import com.anoyi.grpc.config.GrpcProperties;
import com.anoyi.grpc.config.RemoteServer;
import com.anoyi.grpc.service.SerializeService;
import io.grpc.*;
import io.grpc.internal.DnsNameResolverProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GrpcClient {

    /**
     * 中心服务名称
     */
    private static final String CENTRE_SERVER_NAME = "centreServer";

    /**
     * 中心服务类型，0不使用中心服务，1nginx
     */
    private static int centreServerType = 0;

    private static final Map<String, ServerContext> serverMap = new HashMap<>();

    private final GrpcProperties grpcProperties;

    private final SerializeService serializeService;

    private ClientInterceptor clientInterceptor;

    public GrpcClient(GrpcProperties grpcProperties, SerializeService serializeService) {
        this.grpcProperties = grpcProperties;
        this.serializeService = serializeService;
    }

    public GrpcClient(GrpcProperties grpcProperties, SerializeService serializeService, ClientInterceptor clientInterceptor) {
        this.grpcProperties = grpcProperties;
        this.serializeService = serializeService;
        this.clientInterceptor = clientInterceptor;
    }

    /**
     * 初始化
     */
    public void init(){
        List<RemoteServer> remoteServers = grpcProperties.getRemoteServers();
        if(CollectionUtils.isEmpty(remoteServers) && StringUtils.hasText(grpcProperties.getNginxHost())){
            centreServerType = 1;
            remoteServers = new ArrayList<>();
            RemoteServer nginxServer = new RemoteServer();
            if(grpcProperties.getNginxHost().indexOf(":") > -1) {
                try {
                    String[] addrArr = grpcProperties.getNginxHost().split(":");
                    nginxServer.setHost(addrArr[0]);
                    nginxServer.setPort(Integer.valueOf(addrArr[1]));
                } catch (Exception e) {
                    log.error("nginxHost 参数解析异常...", e);
                }
            }else if(grpcProperties.getNginxHost().indexOf("https://") == 0){
                nginxServer.setHost(grpcProperties.getNginxHost());
                nginxServer.setPort(443);
            }else if(grpcProperties.getNginxHost().indexOf("http://") == 0){
                nginxServer.setHost(grpcProperties.getNginxHost());
                nginxServer.setPort(80);
            }
            if(StringUtils.hasText(nginxServer.getHost())){
                nginxServer.setServer(GrpcClient.CENTRE_SERVER_NAME);
                remoteServers.add(nginxServer);
            }
        }
        if (!CollectionUtils.isEmpty(remoteServers)) {
            for (RemoteServer server : remoteServers) {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(server.getHost(), server.getPort())
                        .defaultLoadBalancingPolicy("round_robin")
                        .nameResolverFactory(new DnsNameResolverProvider())
                        .idleTimeout(3, TimeUnit.MINUTES)
                        .usePlaintext().build();
                if (clientInterceptor != null){
                    Channel newChannel = ClientInterceptors.intercept(channel, clientInterceptor);
                    serverMap.put(server.getServer(), new ServerContext(newChannel, serializeService, grpcProperties));
                }else {
                    Class clazz = grpcProperties.getClientInterceptor();
                    if (clazz == null) {
                        serverMap.put(server.getServer(), new ServerContext(channel, serializeService, grpcProperties));
                    }else {
                        try {
                            ClientInterceptor interceptor = (ClientInterceptor) clazz.newInstance();
                            Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
                            serverMap.put(server.getServer(), new ServerContext(newChannel, serializeService, grpcProperties));
                        } catch (InstantiationException | IllegalAccessException e) {
                            log.warn("ClientInterceptor cannot use, ignoring...");
                            serverMap.put(server.getServer(), new ServerContext(channel, serializeService, grpcProperties));
                        }
                    }
                }
            }
        }
    }

    /**
     * 连接远程服务
     */
    public static ServerContext connect(String serverName, String clazz) {
        ServerContext serverContext = null;
        if(centreServerType == 0){
            if(StringUtils.hasText(serverName)){
                //通过服务名称查找
                serverContext = serverMap.get(serverName);
            }else if(StringUtils.hasText(clazz)){
                //通过包名查找
                for(String key : serverMap.keySet()){
                    if(clazz.indexOf(key) == 0){
                        serverContext = serverMap.get(key);
                        break;
                    }
                }
            }
        }else if(centreServerType == 1){
            serverContext = serverMap.get(GrpcClient.CENTRE_SERVER_NAME);
        }
        return serverContext;
    }

}
