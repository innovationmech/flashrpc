package org.example.flashrpc.common.rpc.spi;

public interface RpcServiceProvider {
    String getServiceName();
    Class<?> getServiceInterface();
    Object getServiceImpl();
}