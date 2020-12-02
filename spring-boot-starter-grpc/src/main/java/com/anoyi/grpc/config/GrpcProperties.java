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
     * withDeadline duration TimeUnit.MILLISECONDS
     */
    private long duration = 30000;

    /**
     * 配置nginx地址
     * 设定此参数后，将通过nginx做负载均衡和服务分发
     * 设定此参数后，RemoteServer参数将失效，不需要再设定它，GrpcService也可以不用设置server（不设定server将通过类路径（包名）区分服务）
     * 发起请求时，通过自定义headers的grpc-clazz和grpc-server参数，在nginx方做服务分发
     * 需要nginx配置配合，具体配置请参考wiki文档：4.1-nginx分发与负载均衡
     */
    private String nginxHost;

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

}