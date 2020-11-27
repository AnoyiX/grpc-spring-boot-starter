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
    private int port = 6565;

    /**
     * client config
     */
    private List<RemoteServer> remoteServers;

    /**
     * withDeadline duration TimeUnit.MILLISECONDS
     */
    private long duration = 30000;

    /**
     * client interceptor
     */
    private Class clientInterceptor;

    /**
     * server interceptor
     */
    private Class serverInterceptor;

}