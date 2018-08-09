### Quick Start

**1、获取 spring-boot-starter-grpc 源码**
```
git clone https://github.com/ChinaSilence/spring-boot-starter-grpc.git
```

**2、安装到本地 Maven 仓库**
```
# spring-boot-starter-grpc/spring-boot-starter-grpc 目录下执行
mvn clean install
```

**3、安装 服务提供方 和 服务调用方 共用的接口模块**
```
# spring-boot-starter-grpc/samples-facade 目录下执行
mvn clean install
```

**4、启动 服务提供方**
```
# spring-boot-starter-grpc/samples-server 目录下执行
mvn spring-boot:run
```

**5、启动 服务调用方**
```
# 新开终端窗口，在 spring-boot-starter-grpc/samples-client 目录下执行
mvn spring-boot:run
```

**6、测试远程调用**
```
curl http://localhost:8081/user/list
```

### How To Use

所有用到 spring-boot-starter-grpc 的模块都需要添加依赖：
```
<dependency>
    <groupId>com.anoyi</groupId>
    <artifactId>spring-boot-starter-grpc</artifactId>
    <version>1.1.3.RELEASE</version>
</dependency>
```

**1、共用 interface 模块**

1.1 示例接口
```
@GrpcService(server = "user")
public interface UserService {

    void insert(UserEntity userEntity);

    void deleteById(Long id);

    void update(UserEntity userEntity);

    UserEntity findById(Long id);

    List<UserEntity> findAll();

}
```
> `server` 字段必填，对应 服务调用方 配置文件中的 `spring.grpc.remote-servers.server` 值


**2、服务提供方使用指南**

2.1 在 application.yml 中添加如下配置：
```
spring:
  grpc:
    enable: true
    port: 6565
```

2.2 添加 interface 模块的依赖，并实现所有接口的所有方法, 示例：

```
@Service
public class UserServiceImpl implements UserService {

    /**
     * 模拟数据库存储用户信息
     */
    private Map<Long, UserEntity> userMap = new ConcurrentHashMap<>();

    @Override
    public void insert(UserEntity userEntity) {
        if (userEntity == null){
            log.warn("insert user fail, userEntity is null!");
            return ;
        }
        userMap.putIfAbsent(userEntity.getId(), userEntity);
    }

    // 其他省略

}

```
> 接口实现类的命名的前缀必须与接口名相同

**3、服务调用方使用指南**

3.1 在 application.yml 中添加如下配置：
```
spring:
  grpc:
    remote-servers:
      - server: user
        host: 127.0.0.1
        port: 6565
      - server: pay
        host: 192.168.0.3
        port: 6565
```
>  注意：`server` 属性在每个服务中唯一，`host` 和 `port` 为服务提供方提供的通信端口

3.2 配置 `@GrpcService` 包扫描路径，示例：
```
@SpringBootApplication
@GrpcServiceScan(basePackages = {"com.anoyi.grpc.facade"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```
> 提示，由于使用了共用的接口工程，spring boot 无法直接扫描当前工程外部的信息，所以需要手动指定 @GrpcService 的包扫描路径，如果 @GrpcService 定义在当前工程内部，则无需配置 @GrpcService

3.3 添加 interface 模块的依赖，在任何 spring boot component 类中使用 @Autowired 注解即可(或者通过构造器注入)，示例：

```
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public UserEntity insertUser(@RequestBody UserEntity userEntity){
        userService.insert(userEntity);
        return userEntity;
    }

    // 省略其他

}

```

**4、元数据 - headers & trailers**

4.1 服务提供方配置，配置示例：
```
spring:
  grpc:
    enable: true
    port: 6565
    server-interceptor: ***.***.**ServerInterceptor  # class 类
```

4.2 服务提供方配置，配置示例：
```
spring:
  grpc:
    client-interceptor: ***.***.**ClientInterceptor  # class 类
    remote-servers:
    - server: user
      host: 127.0.0.1
      port: 6565
```

4.3 官方示例

[https://github.com/grpc/grpc-java/tree/master/examples/src/main/java/io/grpc/examples/header](https://github.com/grpc/grpc-java/tree/master/examples/src/main/java/io/grpc/examples/header)


### 相关文档
- [gRPC - Java QuickStart](https://grpc.io/docs/quickstart/java.html)
- [github : gRPC-java](https://github.com/grpc/grpc-java)
- [github : spring-boot-starter-grpc](https://github.com/ChinaSilence/spring-boot-starter-grpc)
