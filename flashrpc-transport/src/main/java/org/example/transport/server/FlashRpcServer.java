package org.example.transport.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.example.transport.protocol.FlashRpcProtocol;

public class FlashRpcServer {

    private final int port;

    public FlashRpcServer(int port) {
        this.port = port;
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
                                .addLast(new FlashRpcServerHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    private ChannelFuture bindAndWait(ServerBootstrap bootstrap) throws InterruptedException {
        ChannelFuture future = bootstrap.bind(port).sync();
        System.out.println("服务器已在端口 " + port + " 上启动");
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
