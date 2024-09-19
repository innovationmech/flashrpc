package org.example.transport.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.example.rpc.RpcRequest;
import org.example.rpc.RpcResponse;
import org.example.transport.protocol.FlashRpcMessage;
import org.example.transport.protocol.FlashRpcProtocol;
import org.example.transport.service.CalculatorService;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class FlashRpcClient {

    private final String host;
    private final int port;
    private Channel channel;
    private EventLoopGroup group;
    private final AtomicLong messageIdGenerator = new AtomicLong(0);
    private final ConcurrentHashMap<Long, CompletableFuture<FlashRpcMessage>> responseFutures = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FlashRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new FlashRpcProtocol());
                            ch.pipeline().addLast(new FlashRpcClientHandler(responseFutures));
                        }
                    });

            ChannelFuture f = b.connect(host, port).sync();
            channel = f.channel();
            System.out.println("FlashRpc Client connected to " + host + ":" + port);
        } catch (Exception e) {
            group.shutdownGracefully();
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                (proxy, method, args) -> {
                    long messageId = messageIdGenerator.incrementAndGet();
                    RpcRequest request = new RpcRequest(method.getName(), args);
                    byte[] requestBody = objectMapper.writeValueAsBytes(request);

                    FlashRpcMessage message = new FlashRpcMessage();
                    message.setMessageId(messageId);
                    message.setMessageType(FlashRpcMessage.MESSAGE_TYPE_REQUEST);
                    message.setSerializationType(FlashRpcMessage.SERIALIZATION_JSON);
                    message.setBody(requestBody);

                    CompletableFuture<FlashRpcMessage> future = new CompletableFuture<>();
                    responseFutures.put(messageId, future);

                    channel.writeAndFlush(message);
                    FlashRpcMessage response = future.get(); // 等待响应
                    
                    // 解析响应
                    RpcResponse rpcResponse = objectMapper.readValue(response.getBody(), RpcResponse.class);
                    return rpcResponse.getResult();
                }
        );
    }

    public void stop() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

}
