
### 使用说明 【服务端】

1、新建 Spring Boot 应用【非 webflux】

2、添加依赖，需将源码安装到本地 Maven 仓库
```
<dependency>
    <groupId>com.anoyi</groupId>
    <artifactId>spring-boot-starter-grpc</artifactId>
    <version>1.0.0.RELEASE</version>
</dependency>
```

3、配置 grpc，在 application.yml 中添加如下内容：
```
spring:
  grpc:
    enable: true
    port: 6565
```

4、提供 Service 
```
@Service
public class HelloService{

    public String sayHello(){
        return "Hello";
    }

    public String say(String words){
        return words;
    }

}
```


### 使用说明 【测试类】

`GrpcClientTest` 提供了三个测试方法，其他待添加