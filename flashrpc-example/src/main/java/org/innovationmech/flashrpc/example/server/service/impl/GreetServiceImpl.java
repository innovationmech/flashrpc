package org.innovationmech.flashrpc.example.server.service.impl;

import org.innovationmech.flashrpc.example.common.service.GreetService;
import org.innovationmech.flashrpc.example.proto.GreetProto.GreetRequest;
import org.innovationmech.flashrpc.example.proto.GreetProto.GreetResponse;

public class GreetServiceImpl implements GreetService {
    @Override
    public GreetResponse greet(GreetRequest request) {
        String name = request.getMsg();
        return GreetResponse.newBuilder()
            .setMsg("Hello, " + name)
            .build();
    }
}
