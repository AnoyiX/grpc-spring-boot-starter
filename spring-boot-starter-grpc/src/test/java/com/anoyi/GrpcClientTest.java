package com.anoyi;

import com.anoyi.rpc.CommonServiceGrpc;
import com.anoyi.rpc.GrpcService;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.anoyi.grpc.service.GrpcRequest;
import com.anoyi.grpc.service.GrpcResponse;
import com.anoyi.grpc.util.ProtobufUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GrpcClientTest {

    public static void main(String[] args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
        final CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(channel);
        long start = System.currentTimeMillis();
        try {
            // 测试无参函数
            testNoArgs(blockingStub);

            // 测试有参函数
//            Object[] requestArgs = {"Hello world!"};
//            testArgs(blockingStub, requestArgs);

            // 性能测试
//            testPerformance(blockingStub);

        } finally {
            long end = System.currentTimeMillis();
            // 计算调用耗时
            System.err.println("Exec Time : " + (end - start) + "ms");
            // 关闭连接
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    /**
     * 无参函数调用
     */
    private static void testNoArgs(CommonServiceGrpc.CommonServiceBlockingStub blockingStub){
        // 构建请求体
        GrpcRequest grpcRequest = new GrpcRequest();
        grpcRequest.setBeanName("helloService");
        grpcRequest.setMethodName("sayHello");
        byte[] bytes = ProtobufUtils.serialize(grpcRequest);
        GrpcService.Request request = GrpcService.Request.newBuilder().setRequest(ByteString.copyFrom(bytes)).build();

        // 调用
        GrpcService.Response response = blockingStub.handle(request);

        // 响应结果
        GrpcResponse grpcResponse = ProtobufUtils.deserialize(response.getReponse().toByteArray(), GrpcResponse.class);
        System.out.println("status: " + grpcResponse.getStatus());
        System.out.println("message: " + grpcResponse.getMessage());
        System.out.println("result " + grpcResponse.getResult().toString());
    }

    /**
     * 有参函数调用
     */
    private static void testArgs(CommonServiceGrpc.CommonServiceBlockingStub blockingStub, Object[] args){
        // 构建请求体
        GrpcRequest grpcRequest = new GrpcRequest();
        grpcRequest.setBeanName("helloService");
        grpcRequest.setMethodName("say");
        grpcRequest.setArgs(args);
        byte[] bytes = ProtobufUtils.serialize(grpcRequest);
        GrpcService.Request request = GrpcService.Request.newBuilder().setRequest(ByteString.copyFrom(bytes)).build();

        // 调用
        GrpcService.Response response = blockingStub.handle(request);

        // 响应结果
        GrpcResponse grpcResponse = ProtobufUtils.deserialize(response.getReponse().toByteArray(), GrpcResponse.class);
        System.out.println("status: " + grpcResponse.getStatus());
        System.out.println("message: " + grpcResponse.getMessage());
        System.out.println("result " + grpcResponse.getResult().toString());
    }

    /**
     * 性能测试
     */
    private static void testPerformance(CommonServiceGrpc.CommonServiceBlockingStub blockingStub){
        // 线程大小
        ExecutorService executorService = Executors.newFixedThreadPool(50);

        // 执行次数
        for (int i = 0; i < 10000; i++) {
            final Object[] args = {"num - " + i};
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    testArgs(blockingStub, args);
                }
            });
        }
        executorService.shutdown();
        while (!executorService.isTerminated()){

        }
    }

}
