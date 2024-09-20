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
        RpcRequest request = new RpcRequest(serviceInterface.getName(), method.getName(), method.getParameterTypes(), args);
        byte[] requestBody;
        byte serializationType;

        Serializer serializer;
        if (isProtobufMessage(args)) {
            serializer = protobufSerializer;
            serializationType = FlashRpcMessage.SERIALIZATION_PROTOBUF;
        } else {
            serializer = jsonSerializer;
            serializationType = FlashRpcMessage.SERIALIZATION_JSON;
        }

        requestBody = serializer.serialize(request);

        FlashRpcMessage message = new FlashRpcMessage();
        message.setMessageId(messageId);
        message.setMessageType(FlashRpcMessage.MESSAGE_TYPE_REQUEST);
        message.setSerializationType(serializationType);
        message.setBody(requestBody);

        FlashRpcMessage response = client.sendRequest(message).get();

        if (response.getSerializationType() == FlashRpcMessage.SERIALIZATION_PROTOBUF) {
            return protobufSerializer.deserialize(response.getBody(), method.getReturnType());
        } else {
            return jsonSerializer.deserialize(response.getBody(), RpcResponse.class);
        }
    }

    private boolean isProtobufMessage(Object[] args) {
        return args != null && args.length > 0 && args[0] instanceof Message;
    }
}
