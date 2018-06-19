package com.anoyi.grpc.util;

public class ClassNameUtils {

    /**
     * 将 Class 全限定名转化为 beanName
     *
     * 比如：com.anoyi.service.UserService -> userService
     */
    public static String beanName(String className){
        String[] path = className.split("\\.");
        String beanName = path[path.length - 1];
        return Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
    }

}
