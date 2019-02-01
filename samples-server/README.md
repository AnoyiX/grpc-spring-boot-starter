### 构建镜像

1、Maven 打包
```
mvn clean package
```

2、构建 Docker 镜像
```
docker build -t server .
```