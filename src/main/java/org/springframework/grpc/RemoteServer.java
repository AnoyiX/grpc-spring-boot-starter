package org.springframework.grpc;

/**
 * 远程服务
 */
public class RemoteServer {

    // 服务名
    private String server;

    // 主机名
    private String host;

    // 端口号
    private int port;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
