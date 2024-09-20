package org.innovationmech.flashrpc.client;

public class FlashRpcClientBuilder {
    private String host;
    private int port;

    public FlashRpcClientBuilder host(String host) {
        this.host = host;
        return this;
    }

    public FlashRpcClientBuilder port(int port) {
        this.port = port;
        return this;
    }

    public FlashRpcClient build() throws Exception {
        FlashRpcClient client = new FlashRpcClient(host, port);
        client.start();
        return client;
    }
}
