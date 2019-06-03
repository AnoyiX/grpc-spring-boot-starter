package com.anoyi.grpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties(prefix = "spring.grpc")
public class GrpcProperties {

    /**
     *
     */
    public static final int DEFAULT_INVOKE_TIMEOUT = 3;

    /**
     * enable server start
     */
    private boolean enable;

    /**
     * server listen port
     */
    private int port;

    /**
     *
     */
    private int invokeTimeout = DEFAULT_INVOKE_TIMEOUT;

    /**
     *
     */
    private TimeUnit invokeTimeoutUnit = TimeUnit.SECONDS;

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