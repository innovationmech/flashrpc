package org.innovationmech.flashrpc.transport.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.innovationmech.flashrpc.common.rpc.RpcRequest;
import org.innovationmech.flashrpc.common.rpc.RpcResponse;
import org.innovationmech.flashrpc.common.rpc.registry.ServiceRegistry;
import org.innovationmech.flashrpc.transport.config.ConfigurationManager;
import org.innovationmech.flashrpc.transport.protocol.FlashRpcMessage;
import org.innovationmech.flashrpc.transport.serializer.JsonSerializer;
import org.innovationmech.flashrpc.transport.serializer.ProtobufSerializer;
import org.innovationmech.flashrpc.transport.serializer.Serializer;

import java.lang.reflect.Method;

public class FlashRpcServerHandler extends SimpleChannelInboundHandler<FlashRpcMessage> {

    private final ServiceRegistry serviceRegistry;
    private final Serializer jsonSerializer;
    private final Serializer protobufSerializer;

    public FlashRpcServerHandler(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.jsonSerializer = new JsonSerializer();
        this.protobufSerializer = new ProtobufSerializer();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FlashRpcMessage msg) throws Exception {
        System.out.println("Received message: " + msg);

        if (msg.getMessageType() == FlashRpcMessage.MESSAGE_TYPE_REQUEST) {
            handleRpcRequest(ctx, msg);
        } else if (msg.getMessageType() == FlashRpcMessage.MESSAGE_TYPE_HEARTBEAT) {
            handleHeartbeat(ctx, msg);
        }
    }

    private void handleRpcRequest(ChannelHandlerContext ctx, FlashRpcMessage msg) throws Exception {
        Serializer serializer = getSerializer(msg.getSerializationType());
        RpcRequest request = serializer.deserialize(msg.getBody(), RpcRequest.class);

        String serviceName = request.getInterfaceName();
        Object serviceInstance = serviceRegistry.getService(serviceName);
        if (serviceInstance == null) {
            throw new IllegalArgumentException("Service not found: " + serviceName);
        }

        Method method = findMethod(serviceInstance.getClass(), request.getMethodName(), request.getParamTypes());
        RpcResponse response;
        try {
            Object result = method.invoke(serviceInstance, request.getParams());
            response = new RpcResponse(result, null);
        } catch (Exception e) {
            response = new RpcResponse(null, e);
        }

        byte[] responseBody = serializer.serialize(response);

        FlashRpcMessage responseMsg = new FlashRpcMessage();
        responseMsg.setMessageId(msg.getMessageId());
        responseMsg.setMessageType(FlashRpcMessage.MESSAGE_TYPE_RESPONSE);
        responseMsg.setSerializationType(msg.getSerializationType());
        responseMsg.setBody(responseBody);

        ctx.writeAndFlush(responseMsg);
    }

    private Serializer getSerializer(byte serializationType) {
        if (serializationType == 0) {
            serializationType = ConfigurationManager.getDefaultSerializationType();
        }
        return switch (serializationType) {
            case FlashRpcMessage.SERIALIZATION_JSON -> jsonSerializer;
            case FlashRpcMessage.SERIALIZATION_PROTOBUF -> protobufSerializer;
            default -> throw new IllegalArgumentException("Unsupported serialization type: " + serializationType);
        };
    }

    private Method findMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) throws NoSuchMethodException {
        return clazz.getMethod(methodName, paramTypes);
    }

    private void handleHeartbeat(ChannelHandlerContext ctx, FlashRpcMessage msg) {
        FlashRpcMessage heartbeatResponse = new FlashRpcMessage();
        heartbeatResponse.setMessageId(msg.getMessageId());
        heartbeatResponse.setMessageType(FlashRpcMessage.MESSAGE_TYPE_HEARTBEAT);
        heartbeatResponse.setSerializationType(msg.getSerializationType());
        heartbeatResponse.setBody(new byte[0]); // 心跳响应通常不需要包含数据

        ctx.writeAndFlush(heartbeatResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
