package org.example.flashrpc.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.example.flashrpc.transport.protocol.FlashRpcProtocol;
import org.example.flashrpc.transport.protocol.FlashRpcMessage;

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

    FlashRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    void start() throws Exception {
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

    public <T> T create(Class<T> serviceInterface) {
        return FlashRpcProxy.create(serviceInterface, this);
    }

    CompletableFuture<FlashRpcMessage> sendRequest(FlashRpcMessage message) {
        CompletableFuture<FlashRpcMessage> future = new CompletableFuture<>();
        responseFutures.put(message.getMessageId(), future);
        channel.writeAndFlush(message);
        return future;
    }

    public void stop() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    public long getNextMessageId() {
        return messageIdGenerator.incrementAndGet();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
