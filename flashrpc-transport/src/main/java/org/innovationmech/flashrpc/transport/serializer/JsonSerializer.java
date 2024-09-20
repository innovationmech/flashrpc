package org.innovationmech.flashrpc.transport.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer implements Serializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) throws Exception {
        return objectMapper.writeValueAsBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        return objectMapper.readValue(data, clazz);
    }
}
