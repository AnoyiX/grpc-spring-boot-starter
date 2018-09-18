package com.anoyi.grpc.service.impl;

import com.anoyi.grpc.exception.GrpcException;
import com.anoyi.grpc.service.GrpcRequest;
import com.anoyi.grpc.service.GrpcResponse;
import com.anoyi.grpc.service.SerializeService;
import com.anoyi.rpc.GrpcService;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.google.protobuf.ByteString;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * sofa-hessian 序列化/反序列化工具
 *
 * 参考：
 * https://github.com/alipay/sofa-rpc/blob/master/extension-impl/codec-sofa-hessian/src/main/java/com/alipay/sofa/rpc/codec/sofahessian/SofaHessianSerializer.java
 */
public class SofaHessianSerializeService implements SerializeService {

    private SerializerFactory serializerFactory = new SerializerFactory();

    @Override
    public ByteString serialize(GrpcResponse response) {
        byte[] bytes = encode(response);
        return ByteString.copyFrom(bytes);
    }

    @Override
    public ByteString serialize(GrpcRequest request) {
        byte[] bytes = encode(request);
        return ByteString.copyFrom(bytes);
    }

    @Override
    public GrpcRequest deserialize(GrpcService.Request request) {
        byte[] bytes = request.getRequest().toByteArray();
        UnsafeByteArrayInputStream inputStream = new UnsafeByteArrayInputStream(bytes);
        try {
            Hessian2Input input = new Hessian2Input(inputStream);
            input.setSerializerFactory(serializerFactory);
            GrpcRequest grpcRequest = (GrpcRequest) input.readObject();
            input.close();
            return grpcRequest;
        } catch (IOException e) {
            throw new GrpcException("sofa-hessian deserialize fail: " + e.getMessage());
        }
    }

    @Override
    public GrpcResponse deserialize(GrpcService.Response response) {
        byte[] bytes = response.getResponse().toByteArray();
        UnsafeByteArrayInputStream inputStream = new UnsafeByteArrayInputStream(bytes);
        try {
            Hessian2Input input = new Hessian2Input(inputStream);
            input.setSerializerFactory(serializerFactory);
            GrpcResponse grpcResponse = (GrpcResponse) input.readObject();
            input.close();
            return grpcResponse;
        } catch (IOException e) {
            throw new GrpcException("sofa-hessian deserialize fail: " + e.getMessage());
        }
    }

    private byte[] encode(Object object) {
        UnsafeByteArrayOutputStream byteArray = new UnsafeByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArray);
        try {
            output.setSerializerFactory(serializerFactory);
            output.writeObject(object);
            output.close();
            return byteArray.toByteArray();
        } catch (Exception e) {
            throw new GrpcException("sofa-hessian serialize fail: " + e.getMessage());
        }
    }

    /**
     * 参考：
     * https://github.com/alipay/sofa-rpc/blob/master/core/common/src/main/java/com/alipay/sofa/rpc/common/struct/UnsafeByteArrayInputStream.java
     */
    @NotThreadSafe
    class UnsafeByteArrayInputStream extends InputStream {

        byte[] mData;

        int mPosition, mLimit, mMark = 0;

        UnsafeByteArrayInputStream(byte[] buf) {
            this(buf, 0, buf.length);
        }

        UnsafeByteArrayInputStream(byte[] buf, int offset, int length) {
            mData = buf;
            mPosition = mMark = offset;
            mLimit = Math.min(offset + length, buf.length);
        }

        @Override
        public int read() {
            return (mPosition < mLimit) ? (mData[mPosition++] & 0xff) : -1;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (b == null) {
                throw new NullPointerException();
            }
            if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            }
            if (mPosition >= mLimit) {
                return -1;
            }
            if (mPosition + len > mLimit) {
                len = mLimit - mPosition;
            }
            if (len <= 0) {
                return 0;
            }
            System.arraycopy(mData, mPosition, b, off, len);
            mPosition += len;
            return len;
        }

        @Override
        public long skip(long len) {
            if (mPosition + len > mLimit) {
                len = mLimit - mPosition;
            }
            if (len <= 0) {
                return 0;
            }
            mPosition += len;
            return len;
        }

        @Override
        public int available() {
            return mLimit - mPosition;
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void mark(int readAheadLimit) {
            mMark = mPosition;
        }

        @Override
        public void reset() {
            mPosition = mMark;
        }

        @Override
        public void close() throws IOException {
        }

    }

    /**
     * 参考：
     * https://github.com/alipay/sofa-rpc/blob/master/core/common/src/main/java/com/alipay/sofa/rpc/common/struct/UnsafeByteArrayOutputStream.java
     */
    @NotThreadSafe
    class UnsafeByteArrayOutputStream extends OutputStream {
        byte[] mBuffer;

        int mCount;

        UnsafeByteArrayOutputStream() {
            this(32);
        }

        UnsafeByteArrayOutputStream(int size) {
            if (size < 0) {
                throw new IllegalArgumentException("Negative initial size: " + size);
            }
            mBuffer = new byte[size];
        }

        @Override
        public void write(int b) {
            int newcount = mCount + 1;
            if (newcount > mBuffer.length) {
                mBuffer = Arrays.copyOf(mBuffer, Math.max(mBuffer.length << 1, newcount));
            }
            mBuffer[mCount] = (byte) b;
            mCount = newcount;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return;
            }
            int newcount = mCount + len;
            if (newcount > mBuffer.length) {
                mBuffer = Arrays.copyOf(mBuffer, Math.max(mBuffer.length << 1, newcount));
            }
            System.arraycopy(b, off, mBuffer, mCount, len);
            mCount = newcount;
        }

        byte[] toByteArray() {
            return Arrays.copyOf(mBuffer, mCount);
        }

        @Override
        public String toString() {
            return new String(mBuffer, 0, mCount);
        }

        @Override
        public void close() throws IOException {
        }

    }

}
