package org.innovationmech.flashrpc.example.server;

import org.innovationmech.flashrpc.common.rpc.spi.RpcServiceProvider;
import org.innovationmech.flashrpc.example.common.service.UserService;

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