package org.example.flashrpc.example.server;

import org.example.flashrpc.transport.server.FlashRpcServer;

public class ExampleServer {
    public static void main(String[] args) throws Exception {
        FlashRpcServer server = new FlashRpcServer(8080);
        server.start();
    }
}
