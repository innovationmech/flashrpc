package org.example.transport.protocol;

public class FlashRpcMessage {

    public static final byte MESSAGE_TYPE_REQUEST = 1;
    public static final byte MESSAGE_TYPE_RESPONSE = 2;
    public static final byte MESSAGE_TYPE_HEARTBEAT = 3;

    public static final byte SERIALIZATION_JSON = 1;
    public static final byte SERIALIZATION_PROTOBUF = 2;

    private byte messageType;
    private byte serializationType;
    private long messageId;
    private byte[] body;

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public byte getSerializationType() {
        return serializationType;
    }

    public void setSerializationType(byte serializationType) {
        this.serializationType = serializationType;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
