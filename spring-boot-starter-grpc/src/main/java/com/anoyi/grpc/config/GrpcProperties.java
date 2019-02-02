package com.anoyi.grpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "spring.grpc")
public class GrpcProperties {

    /**
     * enable server start
     */
    private boolean enable;

    /**
     * server listen port
     */
    private int port;

    /**
     * client config
     */
    private List<RemoteServer> remoteServers;

    /**
     * client interceptor
     */
    private Class clientInterceptor;

    /**
     * server interceptor
     */
    private Class serverInterceptor;

    /**
     * enable refresh dns record
     */
    private boolean enableNameResolverRefresh;

    /**
     * refresher init delay (s)
     */
    private int nameResolverInitialDelay = 30;

    /**
     * refresher period (s)
     */
    private int nameResolverPeriod = 30;

}