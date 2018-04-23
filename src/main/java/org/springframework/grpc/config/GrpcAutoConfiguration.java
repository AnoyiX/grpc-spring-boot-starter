package org.springframework.grpc.config;

import io.grpc.ServerBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.grpc.GrpcClient;
import org.springframework.grpc.GrpcServerRunner;
import org.springframework.grpc.service.CommonService;

@Configuration
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcAutoConfiguration {

    private final AbstractApplicationContext applicationContext;

    private final GrpcProperties grpcProperties;

    public GrpcAutoConfiguration(AbstractApplicationContext applicationContext, GrpcProperties grpcProperties){
        this.applicationContext = applicationContext;
        this.grpcProperties = grpcProperties;
    }

    @Bean
    public CommonService commonService(){
        return new CommonService(applicationContext);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.grpc.enable", havingValue="true")
    public GrpcServerRunner grpcServerRunner() {
        return new GrpcServerRunner(ServerBuilder.forPort(grpcProperties.getPort()), commonService());
    }

    @Bean
    public GrpcClient grpcClient(){
        return new GrpcClient(grpcProperties);
    }

}