package org.innovationmech.flashrpc.example.server.service;

import org.innovationmech.flashrpc.common.rpc.spi.RpcServiceProvider;
import org.innovationmech.flashrpc.example.common.service.GreetService;
import org.innovationmech.flashrpc.example.server.service.impl.GreetServiceImpl;

public class GreetServiceProvider implements RpcServiceProvider {
    @Override
    public String getServiceName() {
        return GreetService.class.getName();
    }

    @Override
    public Class<?> getServiceInterface() {
        return GreetService.class;
    }

    @Override
    public Object getServiceImpl() {
        return new GreetServiceImpl();
    }
}
