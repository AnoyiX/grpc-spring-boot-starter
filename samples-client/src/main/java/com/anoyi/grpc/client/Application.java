package com.anoyi.grpc.client;

import com.anoyi.grpc.annotation.GrpcServiceScan;
import com.anoyi.grpc.service.CodecService;
import com.anoyi.grpc.service.impl.FastJSONCodecService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
@GrpcServiceScan(basePackages = {"com.anoyi.grpc.facade"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @Primary
    public CodecService codecService(){
        return new FastJSONCodecService();
    }

}