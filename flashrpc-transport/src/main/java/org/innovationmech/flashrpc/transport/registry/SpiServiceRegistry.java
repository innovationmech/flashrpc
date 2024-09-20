package org.innovationmech.flashrpc.transport.registry;

import org.innovationmech.flashrpc.common.rpc.registry.ServiceRegistry;
import org.innovationmech.flashrpc.common.rpc.spi.RpcServiceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class SpiServiceRegistry implements ServiceRegistry {
    private final Map<String, Object> services = new HashMap<>();

    public SpiServiceRegistry() {
        loadServices();
    }

    private void loadServices() {
        ServiceLoader<RpcServiceProvider> serviceLoader = ServiceLoader.load(RpcServiceProvider.class);
        for (RpcServiceProvider provider : serviceLoader) {
            registerService(provider.getServiceName(), provider.getServiceImpl());
        }
    }

    @Override
    public void registerService(String serviceName, Object serviceImpl) {
        services.put(serviceName, serviceImpl);
        System.out.println("Registered service: " + serviceName);
    }

    @Override
    public Object getService(String serviceName) {
        return services.get(serviceName);
    }

    @Override
    public Map<String, Object> getAllServices() {
        return new HashMap<>(services);
    }
}