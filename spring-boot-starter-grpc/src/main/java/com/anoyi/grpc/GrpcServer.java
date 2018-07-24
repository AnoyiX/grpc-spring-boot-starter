package com.anoyi.grpc;

import com.anoyi.grpc.config.GrpcProperties;
import com.anoyi.grpc.service.CommonService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import org.springframework.beans.factory.DisposableBean;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * gRPC Server
 */
public class GrpcServer implements DisposableBean {

    private static final Logger log = Logger.getLogger(GrpcServer.class.getName());

    private final GrpcProperties grpcProperties;

    private final CommonService commonService;

    private ServerInterceptor serverInterceptor;

    private Server server;

    public GrpcServer(GrpcProperties grpcProperties, CommonService commonService) {
        this.grpcProperties = grpcProperties;
        this.commonService = commonService;
    }

    public GrpcServer(GrpcProperties grpcProperties, CommonService commonService, ServerInterceptor serverInterceptor) {
        this.grpcProperties = grpcProperties;
        this.commonService = commonService;
        this.serverInterceptor = serverInterceptor;
    }

    /**
     * 启动服务
     * @throws Exception 异常
     */
    public void start() throws Exception{
        int port = grpcProperties.getPort();
        if (serverInterceptor != null){
            server = ServerBuilder.forPort(port).addService(ServerInterceptors.intercept(commonService, serverInterceptor)).build().start();
        }else {
            Class clazz = grpcProperties.getServerInterceptor();
            if (clazz == null){
                server = ServerBuilder.forPort(port).addService(commonService).build().start();
            }else {
                server = ServerBuilder.forPort(port).addService(ServerInterceptors.intercept(commonService, (ServerInterceptor) clazz.newInstance())).build().start();
            }
        }
        log.info("gRPC Server started, listening on port " + server.getPort());
        startDaemonAwaitThread();
    }

    /**
     * 销毁
     */
    public void destroy() {
        Optional.ofNullable(server).ifPresent(Server::shutdown);
        log.info("gRPC server stopped.");
    }

    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread(()->{
            try {
                GrpcServer.this.server.awaitTermination();
            } catch (InterruptedException e) {
                log.warning("gRPC server stopped." + e.getMessage());
            }
        });
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

}