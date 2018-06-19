package com.anoyi.grpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Inherited
@Retention(RUNTIME)
public @interface GrpcService {

    /**
     * remote server name. Required.
     */
    String server() default "";

    /**
     * SpringContext beans name.
     * If not fill in, will match through Class name.
     */
    String bean() default  "";

}