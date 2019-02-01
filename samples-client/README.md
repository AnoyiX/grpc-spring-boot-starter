### 构建镜像

1、修改 src/main/resources/application.yaml 文件为：
```
server:
  port: 8081

spring:
  grpc:
    remote-servers:
    - server: user
      host: server
      port: 6565
```

2、Maven 打包
```
mvn clean package
```

3、构建 Docker 镜像
```
docker build -t client .
```