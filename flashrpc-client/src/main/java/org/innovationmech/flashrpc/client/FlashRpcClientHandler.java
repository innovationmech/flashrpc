package org.innovationmech.flashrpc.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.innovationmech.flashrpc.transport.protocol.FlashRpcMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class FlashRpcClientHandler extends SimpleChannelInboundHandler<FlashRpcMessage> {
    private final ConcurrentHashMap<Long, CompletableFuture<FlashRpcMessage>> responseFutures;

    public FlashRpcClientHandler(ConcurrentHashMap<Long, CompletableFuture<FlashRpcMessage>> responseFutures) {
        this.responseFutures = responseFutures;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FlashRpcMessage msg) throws Exception {
        CompletableFuture<FlashRpcMessage> future = responseFutures.remove(msg.getMessageId());
        if (future != null) {
            future.complete(msg); // 将 CompletableFuture 标记为已完成，并传递响应消息
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
