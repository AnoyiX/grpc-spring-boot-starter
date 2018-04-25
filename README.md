### gRPC 简介
**gRPC** 是一个现代开源的高性能 RPC 框架，可以在任何环境下运行。它可以有效地将数据中心内和跨数据中心的服务与可插拔支持进行负载均衡、跟踪、健康检查和认证。它也适用于分布式计算，将不同设备、移动应用程序和浏览器连接到后端服务。

**主要使用场景：**
- 在微服务架构中有效地连接多个服务
- 将移动设备、浏览器客户端连接到后端服务
- 生成高效的客户端库

**核心功能：**
- 10 种语言的客户端库支持
- 高效、简单的服务定义框架
- 基于 http/2 传输的双向流式传输
- 可插拔的认证、跟踪、负载均衡和健康检查

![](https://upload-images.jianshu.io/upload_images/3424642-1cca7942610e13c4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


### Spring Boot 快速集成 gRPC

**1、获取 spring-boot-starter-grpc 源码**
```
git clone https://github.com/ChinaSilence/spring-boot-starter-grpc.git
```

**2、安装到本地 Maven 仓库**
```
mvn install
```

**3、在 Spring Boot 工程中添加依赖**
```
<dependency>
    <groupId>com.anoyi</groupId>
    <artifactId>spring-boot-starter-grpc</artifactId>
    <version>1.0.0.RELEASE</version>
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

4.3 示例：gRPC 客户端，在 `application.yml` 中添加配置
```
spring:
  grpc:
    remote-servers:
      - host: localhost
        port: 6565
      - host: 192.168.0.3
        port: 6565
```

4.4 远程服务调用
> 远程服务调用需要知道远程服务的：地址、端口号、服务类、类方法、方法参数，该 starter 定义了 `GrpcRequest` 和 `GrpcResponse`。
```
public class GrpcRequest {

    /**
     * service 类名
     */
    private String beanName;

    /**
     * service 方法名
     */
    private String methodName;

    /**
     * service 方法参数
     */
    private Object[] args;

    // 略 setter / getter...
}
```
```
public class GrpcResponse {

    /**
     * 响应状态：0 - 成功， 1 - 失败
     */
    private int status;

    /**
     * 返回结果
     */
    private Object result;

    // 略 setter / getter...
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
        return "Hello " + words;
    }

}
```
4.4.2 【注解方式】示例：客户端调用服务
```
import org.springframework.grpc.annotation.GrpcService;

/**
 * 使用 @GprcService 注解定义远程服务，server 指定远程服务名，必须在 application.yml 中定义才能使用
 * 方法名 、参数 、 返回结果 必须与服务提供方一致
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
```
import com.alibaba.fastjson.JSON;
import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.grpc.service.GrpcRequest;

import java.util.concurrent.TimeUnit;

public class GrpcClient {

    private final ManagedChannel channel;
    private final CommonServiceGrpc.CommonServiceBlockingStub blockingStub;

    public GrpcClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }

    private GrpcClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = CommonServiceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) throws Exception {
        GrpcClient client = new GrpcClient("localhost", 6565);
        try {
            for (int i = 0; i < 100; i++) {
                String words = "world - " + i;
                client.say(words);
            }
        } finally {
            client.shutdown();
        }
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void say(String words) {
        GrpcRequest grpcRequest = new GrpcRequest();
        grpcRequest.setBeanName("helloService");
        grpcRequest.setMethodName("say");
        Object[] params = {words};
        grpcRequest.setArgs(params);
        System.out.println("远程调用 " + grpcRequest.getServiceBeanName() + "." + grpcRequest.getServiceMethodName() + " ");
        byte[] bytes = ProtobufUtils.serialize(grpcRequest);
        GrpcService.Request request = GrpcService.Request.newBuilder().setRequest(ByteString.copyFrom(bytes)).build();
        GrpcService.Response response = blockingStub.handle(request);
        System.out.println("远程调用结果: " + response.getReponse());
    }

}
```

### 相关文档
- [gRPC - Java QuickStart](https://grpc.io/docs/quickstart/java.html)
- [github : gRPC-java](https://github.com/grpc/grpc-java)
- [github : spring-boot-starter-grpc](https://github.com/ChinaSilence/spring-boot-starter-grpc)