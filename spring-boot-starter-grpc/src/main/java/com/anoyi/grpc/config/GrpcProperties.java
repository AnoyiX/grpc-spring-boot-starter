package com.anoyi.grpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "spring.grpc")
public class GrpcProperties {

    private boolean enable;

    private int port;

    private List<RemoteServer> remoteServers;

    private Class clientInterceptor;

    private Class serverInterceptor;

}