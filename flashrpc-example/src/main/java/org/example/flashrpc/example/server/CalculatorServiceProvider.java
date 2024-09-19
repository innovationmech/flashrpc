package org.example.flashrpc.example.server;

import org.example.flashrpc.common.rpc.spi.RpcServiceProvider;
import org.example.flashrpc.example.common.service.CalculatorService;
import org.example.flashrpc.example.server.service.impl.CalculatorServiceImpl;

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
