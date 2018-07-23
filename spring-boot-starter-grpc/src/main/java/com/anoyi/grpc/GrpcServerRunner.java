package com.anoyi.grpc;

import com.anoyi.grpc.config.GrpcProperties;
import com.anoyi.grpc.service.CommonService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * gRPC Server 启动器
 */
public class GrpcServerRunner implements CommandLineRunner, DisposableBean {

    private static final Logger log = Logger.getLogger(GrpcServerRunner.class.getName());

    private final GrpcProperties grpcProperties;

    private final CommonService commonService;

    private Server server;

    public GrpcServerRunner(GrpcProperties grpcProperties, CommonService commonService) {
        this.grpcProperties = grpcProperties;
        this.commonService = commonService;
    }

    public void run(String... args) throws Exception {
        int port = grpcProperties.getPort();
        Class clazz = grpcProperties.getServerInterceptor();
        if (clazz == null){
            server = ServerBuilder.forPort(port).addService(commonService).build().start();
        }else {
            server = ServerBuilder.forPort(port).addService(ServerInterceptors.intercept(commonService, (ServerInterceptor) clazz.newInstance())).build().start();
        }
        log.info("gRPC Server started, listening on port " + server.getPort());
        startDaemonAwaitThread();
    }

    public void destroy() {
        Optional.ofNullable(server).ifPresent(Server::shutdown);
        log.info("gRPC server stopped.");
    }

    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread(()->{
            try {
                GrpcServerRunner.this.server.awaitTermination();
            } catch (InterruptedException e) {
                log.warning("gRPC server stopped." + e.getMessage());
            }
        });
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

}