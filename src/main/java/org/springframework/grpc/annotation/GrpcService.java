package org.springframework.grpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Inherited
@Retention(RUNTIME)
public @interface GrpcService {

    // remote server hostname
    String server() default "";

    // @Service annotated bean's name
    String name() default  "";

}