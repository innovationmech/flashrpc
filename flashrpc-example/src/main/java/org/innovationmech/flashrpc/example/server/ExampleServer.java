package org.innovationmech.flashrpc.example.server;

import org.innovationmech.flashrpc.transport.server.FlashRpcServer;

public class ExampleServer {
    public static void main(String[] args) throws Exception {
        FlashRpcServer server = new FlashRpcServer(8080);
        server.start();
    }
}
