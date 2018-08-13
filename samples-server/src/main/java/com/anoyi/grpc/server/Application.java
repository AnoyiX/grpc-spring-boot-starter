package com.anoyi.grpc.server;

import com.anoyi.grpc.service.CodecService;
import com.anoyi.grpc.service.impl.FastJSONCodecService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
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
