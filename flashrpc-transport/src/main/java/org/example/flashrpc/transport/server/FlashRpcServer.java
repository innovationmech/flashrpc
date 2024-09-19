package org.example.flashrpc.transport.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.example.flashrpc.common.rpc.registry.ServiceRegistry;
import org.example.flashrpc.transport.protocol.FlashRpcProtocol;
import org.example.flashrpc.transport.registry.SpiServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlashRpcServer {


    private static final Logger logger = LoggerFactory.getLogger(FlashRpcServer.class);

    private final int port;
    private final ServiceRegistry serviceRegistry;

    public FlashRpcServer(int port) {
        this(port, new SpiServiceRegistry());
    }

    public FlashRpcServer(int port, ServiceRegistry serviceRegistry) {
        this.port = port;
        this.serviceRegistry = serviceRegistry;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = configureServerBootstrap(bossGroup, workerGroup);
            ChannelFuture future = bindAndWait(bootstrap);
            waitForShutdown(future);
        } finally {
            shutdownGracefully(bossGroup, workerGroup);
        }
    }

    private ServerBootstrap configureServerBootstrap(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new FlashRpcProtocol())
                                .addLast(new FlashRpcServerHandler(serviceRegistry));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    private ChannelFuture bindAndWait(ServerBootstrap bootstrap) throws InterruptedException {
        ChannelFuture future = bootstrap.bind(port).sync();
        logger.info("服务器已在端口 {} 上启动", port);
        return future;
    }

    private void waitForShutdown(ChannelFuture future) throws InterruptedException {
        future.channel().closeFuture().sync();
    }

    private void shutdownGracefully(EventLoopGroup... groups) {
        for (EventLoopGroup group : groups) {
            if (group != null) {
                group.shutdownGracefully();
            }
        }
    }
}
