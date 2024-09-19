package org.example.transport.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.rpc.RpcRequest;
import org.example.rpc.RpcResponse;
import org.example.transport.protocol.FlashRpcMessage;

import java.lang.reflect.Method;
import java.util.Map;

public class FlashRpcServerHandler extends SimpleChannelInboundHandler<FlashRpcMessage> {

    private final Map<String, Object> serviceInstances;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FlashRpcServerHandler(Map<String, Object> serviceInstances) {
        this.serviceInstances = serviceInstances;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FlashRpcMessage msg) throws Exception {
        System.out.println("Received message: " + msg);

        if (msg.getMessageType() == FlashRpcMessage.MESSAGE_TYPE_REQUEST) {
            String requestBody = new String(msg.getBody());
            RpcRequest request = objectMapper.readValue(requestBody, RpcRequest.class);

            String interfaceName = request.getInterfaceName();
            Object serviceInstance = serviceInstances.get(interfaceName);
            if (serviceInstance == null) {
                throw new IllegalArgumentException("Service not found: " + interfaceName);
            }

            Method method = findMethod(serviceInstance.getClass(), request.getMethodName(), request.getParamTypes());
            RpcResponse response;
            try {
                Object result = method.invoke(serviceInstance, request.getParams());
                response = new RpcResponse(result, null);
            } catch (Exception e) {
                response = new RpcResponse(null, e);
            }

            byte[] responseBody = objectMapper.writeValueAsBytes(response);

            FlashRpcMessage responseMsg = new FlashRpcMessage();
            responseMsg.setMessageId(msg.getMessageId());
            responseMsg.setMessageType(FlashRpcMessage.MESSAGE_TYPE_RESPONSE);
            responseMsg.setSerializationType(msg.getSerializationType());
            responseMsg.setBody(responseBody);

            ctx.writeAndFlush(responseMsg);
        }
    }

    private Method findMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) throws NoSuchMethodException {
        return clazz.getMethod(methodName, paramTypes);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
