package org.innovationmech.flashrpc.transport.serializer;

import com.google.protobuf.MessageLite;
import java.lang.reflect.Method;

public class ProtobufSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) throws Exception {
        if (obj instanceof MessageLite) {
            return ((MessageLite) obj).toByteArray();
        }
        throw new IllegalArgumentException("Object must be a Protobuf message");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (MessageLite.class.isAssignableFrom(clazz)) {
            Method parseFromMethod = clazz.getMethod("parseFrom", byte[].class);
            return (T) parseFromMethod.invoke(null, data);
        }
        throw new IllegalArgumentException("Class must be a Protobuf message type");
    }
}
