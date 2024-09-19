package org.example.flashrpc.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.flashrpc.common.rpc.RpcRequest;
import org.example.flashrpc.common.rpc.RpcResponse;
import org.example.flashrpc.transport.protocol.FlashRpcMessage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

public class FlashRpcProxy implements InvocationHandler {
    private final FlashRpcClient client;
    private final Class<?> serviceInterface;
    private final AtomicLong messageIdGenerator = new AtomicLong(0);
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        byte[] requestBody = objectMapper.writeValueAsBytes(request);

        FlashRpcMessage message = new FlashRpcMessage();
        message.setMessageId(messageId);
        message.setMessageType(FlashRpcMessage.MESSAGE_TYPE_REQUEST);
        message.setSerializationType(FlashRpcMessage.SERIALIZATION_JSON);
        message.setBody(requestBody);

        FlashRpcMessage response = client.sendRequest(message).get();

        RpcResponse rpcResponse = objectMapper.readValue(response.getBody(), RpcResponse.class);
        if (rpcResponse.hasException()) {
            throw rpcResponse.getException();
        }
        return rpcResponse.getResult();
    }
}
