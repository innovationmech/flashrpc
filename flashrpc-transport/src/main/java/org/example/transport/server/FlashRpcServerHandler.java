package org.example.transport.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.rpc.RpcRequest;
import org.example.rpc.RpcResponse;
import org.example.transport.protocol.FlashRpcMessage;
import org.example.transport.service.CalculatorService;
import org.example.transport.service.impl.CalculatorServiceImpl;

import java.lang.reflect.Method;

public class FlashRpcServerHandler extends SimpleChannelInboundHandler<FlashRpcMessage> {

    private final CalculatorService calculatorService = new CalculatorServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FlashRpcMessage msg) throws Exception {
        System.out.println("Received message: " + msg);

        if (msg.getMessageType() == FlashRpcMessage.MESSAGE_TYPE_REQUEST) {
            // 解析请求
            String requestBody = new String(msg.getBody());
            RpcRequest request = objectMapper.readValue(requestBody, RpcRequest.class);

            // 调用服务方法
            Method method = CalculatorService.class.getMethod(request.getMethodName(), int.class, int.class);
            Object[] params = request.getParams();
            if (params.length == 2 && params[0] instanceof Integer && params[1] instanceof Integer) {
                Object result = method.invoke(calculatorService, params[0], params[1]);
                // Create and send response
                // 创建响应
                RpcResponse response = new RpcResponse(result);
                byte[] responseBody = objectMapper.writeValueAsBytes(response);

                FlashRpcMessage responseMsg = new FlashRpcMessage();
                responseMsg.setMessageId(msg.getMessageId());
                responseMsg.setMessageType(FlashRpcMessage.MESSAGE_TYPE_RESPONSE);
                responseMsg.setSerializationType(msg.getSerializationType());
                responseMsg.setBody(responseBody);

                ctx.writeAndFlush(responseMsg);
            } else {
                throw new IllegalArgumentException("Invalid method parameters");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
