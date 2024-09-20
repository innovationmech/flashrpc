package org.innovationmech.flashrpc.common.rpc.spi;

public interface RpcServiceProvider {
    String getServiceName();
    Class<?> getServiceInterface();
    Object getServiceImpl();
}