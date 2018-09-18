package com.anoyi.grpc.service.impl;

import com.alibaba.fastjson.JSON;
import com.anoyi.grpc.service.SerializeService;
import com.anoyi.grpc.service.GrpcRequest;
import com.anoyi.grpc.service.GrpcResponse;
import com.anoyi.rpc.GrpcService;
import com.google.protobuf.ByteString;

/**
 * FastJSON 序列化/反序列化工具
 */
public class FastJSONSerializeService implements SerializeService {

    @Override
    public ByteString serialize(GrpcResponse response) {
        return ByteString.copyFrom(JSON.toJSONBytes(response));
    }

    @Override
    public ByteString serialize(GrpcRequest request) {
        return ByteString.copyFrom(JSON.toJSONBytes(request));
    }

    @Override
    public GrpcRequest deserialize(GrpcService.Request request) {
        byte[] bytes = request.getRequest().toByteArray();
        return JSON.parseObject(bytes, GrpcRequest.class);
    }

    @Override
    public GrpcResponse deserialize(GrpcService.Response response) {
        byte[] bytes = response.getResponse().toByteArray();
        return JSON.parseObject(bytes, GrpcResponse.class);
    }

}
