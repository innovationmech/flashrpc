package org.innovationmech.flashrpc.example.server.service;

import org.innovationmech.flashrpc.common.rpc.spi.RpcServiceProvider;
import org.innovationmech.flashrpc.example.common.service.UserService;
import org.innovationmech.flashrpc.example.server.service.impl.UserServiceImpl;

public class UserServiceProvider implements RpcServiceProvider {
    @Override
    public String getServiceName() {
        return UserService.class.getName();
    }

    @Override
    public Class<?> getServiceInterface() {
        return UserService.class;
    }

    @Override
    public Object getServiceImpl() {
        return new UserServiceImpl();
    }
}