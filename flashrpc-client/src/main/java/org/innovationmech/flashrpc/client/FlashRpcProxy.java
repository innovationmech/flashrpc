package org.innovationmech.flashrpc.client;

import org.innovationmech.flashrpc.common.rpc.RpcRequest;
import org.innovationmech.flashrpc.common.rpc.RpcResponse;
import org.innovationmech.flashrpc.transport.protocol.FlashRpcMessage;
import com.google.protobuf.Message;
import org.innovationmech.flashrpc.transport.serializer.Serializer;
import org.innovationmech.flashrpc.transport.serializer.ProtobufSerializer;
import org.innovationmech.flashrpc.transport.serializer.JsonSerializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

public class FlashRpcProxy implements InvocationHandler {
    private final FlashRpcClient client;
    private final Class<?> serviceInterface;
    private final AtomicLong messageIdGenerator = new AtomicLong(0);
    private final Serializer protobufSerializer = new ProtobufSerializer();
    private final Serializer jsonSerializer = new JsonSerializer();

    private FlashRpcProxy(Class<?> serviceInterface, FlashRpcClient client) {
        this.serviceInterface = serviceInterface;
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> serviceInterface, FlashRpcClient client) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                new FlashRpcProxy(serviceInterface, client)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long messageId = messageIdGenerator.incrementAndGet();
        byte[] requestBody;
        byte serializationType;

        String serviceName = serviceInterface.getName();
        String methodName = method.getName();

        if (isProtobufMessage(args)) {
            byte[] protoBody = protobufSerializer.serialize(args[0]);
            byte[] serviceNameBytes = serviceName.getBytes();
            byte[] methodNameBytes = methodName.getBytes();
            requestBody = new byte[2 + serviceNameBytes.length + methodNameBytes.length + protoBody.length];
            requestBody[0] = (byte) serviceNameBytes.length;
            requestBody[1] = (byte) methodNameBytes.length;
            System.arraycopy(serviceNameBytes, 0, requestBody, 2, serviceNameBytes.length);
            System.arraycopy(methodNameBytes, 0, requestBody, 2 + serviceNameBytes.length, methodNameBytes.length);
            System.arraycopy(protoBody, 0, requestBody, 2 + serviceNameBytes.length + methodNameBytes.length, protoBody.length);
            serializationType = FlashRpcMessage.SERIALIZATION_PROTOBUF;
        } else {
            RpcRequest request = new RpcRequest(serviceName, methodName, method.getParameterTypes(), args);
            requestBody = jsonSerializer.serialize(request);
            serializationType = FlashRpcMessage.SERIALIZATION_JSON;
        }

        FlashRpcMessage message = new FlashRpcMessage();
        message.setMessageId(messageId);
        message.setMessageType(FlashRpcMessage.MESSAGE_TYPE_REQUEST);
        message.setSerializationType(serializationType);
        message.setBody(requestBody);

        FlashRpcMessage response = client.sendRequest(message).get();

        if (response.getSerializationType() == FlashRpcMessage.SERIALIZATION_PROTOBUF) {
            return protobufSerializer.deserialize(response.getBody(), method.getReturnType());
        } else {
            RpcResponse rpcResponse = (RpcResponse) jsonSerializer.deserialize(response.getBody(), RpcResponse.class);
            if (rpcResponse.getException() != null) {
                throw rpcResponse.getException();
            }
            return rpcResponse.getResult();
        }
    }

    private boolean isProtobufMessage(Object[] args) {
        return args != null && args.length == 1 && args[0] instanceof Message;
    }
}
