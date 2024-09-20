package org.innovationmech.flashrpc.transport.config;

import org.innovationmech.flashrpc.transport.protocol.FlashRpcMessage;

public class ConfigurationManager {
    private static byte defaultSerializationType = FlashRpcMessage.SERIALIZATION_JSON;

    public static byte getDefaultSerializationType() {
        return defaultSerializationType;
    }

    public static void setDefaultSerializationType(byte serializationType) {
        defaultSerializationType = serializationType;
    }
}