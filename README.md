### Spring Boot 快速集成 gRPC

**1、获取 spring-boot-starter-grpc 源码**
```
git clone https://github.com/ChinaSilence/spring-boot-starter-grpc.git
```

**2、安装到本地 Maven 仓库【重要，否则代码报错】**
```
mvn install
```

**3、在 Spring Boot 工程中添加依赖**
```
<dependency>
    <groupId>com.anoyi</groupId>
    <artifactId>spring-boot-starter-grpc</artifactId>
    <version>1.0.1.RELEASE</version>
</dependency>
```

**4、gRPC 使用说明**

4.1 参数配置说明
- spring.grpc.enable 是否启用 gRPC 服务端，默认 `false`
- spring.grpc.port 监听的端口号
- spring.grpc.remote-servers 供客户端调用的服务端列表

4.2 示例：gRPC 服务端，在 `application.yml` 中添加配置
```
spring:
  grpc:
    enable: true
    port: 6565
```

4.3 示例：gRPC 客户端，在 `application.yml` 中添加配置，`server` 属性在每个服务中唯一
```
spring:
  grpc:
    remote-servers:
      - server: user
        host: localhost
        port: 6565
      - server: pay
        host: 192.168.0.3
        port: 6565
```

4.4 远程服务调用
> 远程服务调用需要知道远程服务的：地址、端口号、服务类、类方法、方法参数，
> 该 starter 定义了 `GrpcRequest` 和 `GrpcResponse`。
```
public class GrpcRequest {

    // service 类名
    private String beanName;

    // service 方法名
    private String methodName;

    // service 方法参数
    private Object[] args;

}
```
```
public class GrpcResponse {

    // 响应状态：0 - 成功， 1 - 失败
    private int status;

    // 用于错误描述
    private String message;

    // 返回结果
    private Object result;

}
```

4.4.1 示例：服务端提供服务，与单体 Spring Boot 无差别，即单体 Spring Boot 应用可以无缝集成
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
4.4.2 【注解方式】示例：客户端调用服务
```
import org.springframework.grpc.annotation.GrpcService;

/**
 * 使用 @GprcService 注解定义远程服务，server 指定远程服务名，必须在 application.yml 中定义才能使用，
 * 方法名 、参数 、 返回结果 必须与服务提供方一致，
 * 除了 server 属性，还有 name 属性，name 属性可以指定远程服务的 beanName，
 * 默认情况下，远程 beanName 会自动匹配，例如： @Service 注解 HelloService 类，或者 @Service 注解 HelloServiceImpl 类，都能正常获取到 Bean
 */
@GrpcService(server = "localhost")
public interface HelloService {

    public String sayHello();

    public String say(String words);

}

```

4.4.3 【非注解方式】示例：客户端调用服务
```
    public void test(){
        // 构建请求体
        GrpcRequest grpcRequest = new GrpcRequest();
        grpcRequest.setServiceBeanName("helloService");

        // 无参方法调用
        grpcRequest.setServiceMethodName("sayHello");
        try {
            // 此处服务提供方需要在配置文件中定义，否则无法调用
            GrpcResponse response = GrpcClient.connect("localhost").handle(grpcRequest);
            if (response.getStatus() == GrpcResponseStatus.SUCCESS.getCode()){
                System.out.println(response.getResult());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 有参方法调用
        grpcRequest.setServiceMethodName("say");
        Object[] args = {"hello"};
        grpcRequest.setArgs(args);
        try {
            // 此处服务提供方需要在配置文件中定义，否则无法调用
            GrpcResponse response = GrpcClient.connect("localhost").handle(grpcRequest);
            if (response.getStatus() == GrpcResponseStatus.SUCCESS.getCode()){
                System.out.println(response.getResult());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```

4.4.4 【测试使用】示例：为方便调试，通过原生方式调用远程服务，无需依赖 Spring Boot

[https://github.com/ChinaSilence/spring-boot-starter-grpc/tree/master/src/test/java/com/anoyi](https://github.com/ChinaSilence/spring-boot-starter-grpc/tree/master/src/test/java/com/anoyi)

### 相关文档
- [gRPC - Java QuickStart](https://grpc.io/docs/quickstart/java.html)
- [github : gRPC-java](https://github.com/grpc/grpc-java)
- [github : spring-boot-starter-grpc](https://github.com/ChinaSilence/spring-boot-starter-grpc)