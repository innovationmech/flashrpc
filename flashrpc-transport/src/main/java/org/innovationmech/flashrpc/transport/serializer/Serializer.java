package org.innovationmech.flashrpc.transport.serializer;

public interface Serializer {
    byte[] serialize(Object obj) throws Exception;
    <T> T deserialize(byte[] data, Class<T> clazz) throws Exception;
}