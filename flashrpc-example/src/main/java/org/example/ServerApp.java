package org.example;

import org.example.transport.server.FlashRpcServer;

public class ServerApp {

    public static void main(String[] args) {
        try {
            new FlashRpcServer(8080).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
