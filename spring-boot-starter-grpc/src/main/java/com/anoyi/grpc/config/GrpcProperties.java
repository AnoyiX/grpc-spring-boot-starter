package com.anoyi.grpc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import com.anoyi.grpc.RemoteServer;

import java.util.List;

@ConfigurationProperties(prefix = "spring.grpc")
public class GrpcProperties {

    private boolean enable;

    private int port;

    private List<RemoteServer> remoteServers;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<RemoteServer> getRemoteServers() {
        return remoteServers;
    }

    public void setRemoteServers(List<RemoteServer> remoteServers) {
        this.remoteServers = remoteServers;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}