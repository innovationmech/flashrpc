package org.innovationmech.flashrpc.example.server.service;

import org.innovationmech.flashrpc.common.rpc.spi.RpcServiceProvider;
import org.innovationmech.flashrpc.example.common.service.CalculatorService;
import org.innovationmech.flashrpc.example.server.service.impl.CalculatorServiceImpl;

public class CalculatorServiceProvider implements RpcServiceProvider {
    @Override
    public String getServiceName() {
        return CalculatorService.class.getName();
    }

    @Override
    public Class<?> getServiceInterface() {
        return CalculatorService.class;
    }

    @Override
    public Object getServiceImpl() {
        return new CalculatorServiceImpl();
    }
}
