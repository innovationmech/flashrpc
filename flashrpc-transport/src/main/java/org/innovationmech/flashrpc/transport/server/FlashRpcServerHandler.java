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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;

public class FlashRpcServerHandler extends SimpleChannelInboundHandler<FlashRpcMessage> {

    private static final Logger logger = LoggerFactory.getLogger(FlashRpcServerHandler.class);

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
        logger.info("Received message: {}", msg);
        if (msg.getMessageType() == FlashRpcMessage.MESSAGE_TYPE_REQUEST) {
            handleRpcRequest(ctx, msg);
        } else if (msg.getMessageType() == FlashRpcMessage.MESSAGE_TYPE_HEARTBEAT) {
            handleHeartbeat(ctx, msg);
        }
    }

    private void handleRpcRequest(ChannelHandlerContext ctx, FlashRpcMessage msg) throws Exception {
        Serializer serializer = getSerializer(msg.getSerializationType());
        Object request;
        String serviceName;
        String methodName;
        Object[] params;

        if (msg.getSerializationType() == FlashRpcMessage.SERIALIZATION_PROTOBUF) {
            byte[] body = msg.getBody();
            int serviceNameLength = body[0];
            int methodNameLength = body[1];
            serviceName = new String(body, 2, serviceNameLength);
            methodName = new String(body, 2 + serviceNameLength, methodNameLength);
            
            Object serviceInstance = serviceRegistry.getService(serviceName);
            if (serviceInstance == null) {
                logger.error("Service not found: {}", serviceName);
                throw new IllegalArgumentException("Service not found: " + serviceName);
            }
            
            Method method = findMethodByName(serviceInstance.getClass(), methodName);
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1 || !com.google.protobuf.Message.class.isAssignableFrom(parameterTypes[0])) {
                throw new IllegalArgumentException("Protobuf method must have exactly one parameter of Message type");
            }
            byte[] protoBody = new byte[body.length - (2 + serviceNameLength + methodNameLength)];
            System.arraycopy(body, 2 + serviceNameLength + methodNameLength, protoBody, 0, protoBody.length);
            request = protobufSerializer.deserialize(protoBody, parameterTypes[0]);
            params = new Object[]{request};
        } else {
            RpcRequest rpcRequest = serializer.deserialize(msg.getBody(), RpcRequest.class);
            serviceName = rpcRequest.getInterfaceName();
            methodName = rpcRequest.getMethodName();
            params = rpcRequest.getParams();
        }

        Object serviceInstance = serviceRegistry.getService(serviceName);
        if (serviceInstance == null) {
            logger.error("Service not found: {}", serviceName);
            throw new IllegalArgumentException("Service not found: " + serviceName);
        }

        logger.info("Service instance: {}", serviceInstance.getClass().getName());
        logger.info("Method name: {}", methodName);
        logger.info("Params: {}", Arrays.toString(params));
        logger.info("Param types: {}", Arrays.toString(Arrays.stream(params).map(p -> p != null ? p.getClass().getName() : "null").toArray()));

        Method method = findMethod(serviceInstance.getClass(), methodName, params);
        Object result = method.invoke(serviceInstance, params);

        FlashRpcMessage responseMsg = new FlashRpcMessage();
        responseMsg.setMessageId(msg.getMessageId());
        responseMsg.setMessageType(FlashRpcMessage.MESSAGE_TYPE_RESPONSE);
        responseMsg.setSerializationType(msg.getSerializationType());

        if (msg.getSerializationType() == FlashRpcMessage.SERIALIZATION_PROTOBUF) {
            responseMsg.setBody(protobufSerializer.serialize(result));
        } else {
            RpcResponse response = new RpcResponse(result, null);
            responseMsg.setBody(serializer.serialize(response));
        }

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

    private Method findMethodByName(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Method not found: " + methodName);
    }

    private Method findMethod(Class<?> clazz, String methodName, Object[] params) throws NoSuchMethodException {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == params.length) {
                boolean match = true;
                for (int i = 0; i < params.length; i++) {
                    Class<?> paramType = method.getParameterTypes()[i];
                    Object param = params[i];
                    if (param != null) {
                        if (paramType.isPrimitive()) {
                            if (!isPrimitiveWrapperMatch(paramType, param.getClass())) {
                                match = false;
                                break;
                            }
                        } else if (!paramType.isAssignableFrom(param.getClass())) {
                            match = false;
                            break;
                        }
                    }
                }
                if (match) {
                    return method;
                }
            }
        }
        throw new NoSuchMethodException("Method not found: " + methodName + " with parameters: " + Arrays.toString(params));
    }

    private boolean isPrimitiveWrapperMatch(Class<?> primitiveType, Class<?> wrapperType) {
        return (primitiveType == int.class && wrapperType == Integer.class) ||
               (primitiveType == long.class && wrapperType == Long.class) ||
               (primitiveType == double.class && wrapperType == Double.class) ||
               (primitiveType == float.class && wrapperType == Float.class) ||
               (primitiveType == boolean.class && wrapperType == Boolean.class) ||
               (primitiveType == byte.class && wrapperType == Byte.class) ||
               (primitiveType == char.class && wrapperType == Character.class) ||
               (primitiveType == short.class && wrapperType == Short.class);
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
