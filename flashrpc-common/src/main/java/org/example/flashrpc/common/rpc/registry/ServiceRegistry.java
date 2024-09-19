package org.example.flashrpc.common.rpc.registry;

import java.util.Map;

public interface ServiceRegistry {
    void registerService(String serviceName, Object serviceImpl);
    Object getService(String serviceName);
    Map<String, Object> getAllServices();
}