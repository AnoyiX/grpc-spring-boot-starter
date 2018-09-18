package com.anoyi.grpc.client;

import com.anoyi.grpc.annotation.GrpcServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@GrpcServiceScan(packages = {"com.anoyi.grpc.facade"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}