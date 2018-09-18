package com.anoyi.grpc.annotation;

import com.anoyi.grpc.config.GrpcAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({GrpcAutoConfiguration.ExternalGrpcServiceScannerRegistrar.class})
public @interface GrpcServiceScan {

    /**
     * `@GrpcService` 所注解的包扫描路径
     */
    String[] packages() default {};

}