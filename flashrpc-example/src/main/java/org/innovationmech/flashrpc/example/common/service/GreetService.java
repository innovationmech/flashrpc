package org.innovationmech.flashrpc.example.common.service;


import org.innovationmech.flashrpc.example.proto.GreetProto.GreetRequest;
import org.innovationmech.flashrpc.example.proto.GreetProto.GreetResponse;

public interface GreetService {

    GreetResponse greet(GreetRequest request);
}
