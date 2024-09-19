package org.example.rpc;

public class RpcResponse {

    private Object result;

    public RpcResponse() {}

    public RpcResponse(Object result) {
        this.result = result;
    }

    // Getter and setter
    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }
}
