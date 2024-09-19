package org.example.flashrpc.transport.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class FlashRpcProtocol extends ByteToMessageCodec<FlashRpcMessage> {

    private static final short MAGIC_NUMBER = 0x11;
    private static final byte VERSION = 1;

    @Override
    protected void encode(ChannelHandlerContext ctx, FlashRpcMessage msg, ByteBuf out) {
        out.writeShort(MAGIC_NUMBER);
        out.writeByte(VERSION);
        out.writeByte(msg.getMessageType());
        out.writeByte(msg.getSerializationType());
        out.writeLong(msg.getMessageId());

        byte[] body = msg.getBody();
        out.writeInt(body.length);
        out.writeBytes(body);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 17) { // 2 + 1 + 1 + 1 + 8 + 4
            return;
        }

        in.markReaderIndex();

        short magic = in.readShort();
        if (magic != MAGIC_NUMBER) {
            in.resetReaderIndex();
            throw new IllegalArgumentException("Invalid magic number: " + magic);
        }

        byte version = in.readByte();
        byte messageType = in.readByte();
        byte serializationType = in.readByte();
        long messageId = in.readLong();
        int bodyLength = in.readInt();

        if (in.readableBytes() < bodyLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] body = new byte[bodyLength];
        in.readBytes(body);

        FlashRpcMessage msg = new FlashRpcMessage();
        msg.setMessageType(messageType);
        msg.setSerializationType(serializationType);
        msg.setMessageId(messageId);
        msg.setBody(body);

        out.add(msg);
    }
}
