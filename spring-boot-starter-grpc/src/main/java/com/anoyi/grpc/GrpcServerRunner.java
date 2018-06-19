package com.anoyi.grpc;

import com.anoyi.grpc.service.CommonService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * gRPC Server 启动器
 */
public class GrpcServerRunner implements CommandLineRunner, DisposableBean {

    private static final Logger log = Logger.getLogger(GrpcServerRunner.class.getName());

    private final ServerBuilder<?> serverBuilder;

    private final CommonService commonService;

    private Server server;

    public GrpcServerRunner(ServerBuilder<?> serverBuilder, CommonService commonService) {
        this.serverBuilder = serverBuilder;
        this.commonService = commonService;
    }

    public void run(String... args) throws Exception {
        server = serverBuilder.addService(commonService).build().start();
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