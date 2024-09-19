package org.example.rpc;

public class RpcRequest {

    private String methodName;
    private Object[] params;

    public RpcRequest() {}

    public RpcRequest(String methodName, Object[] params) {
        this.methodName = methodName;
        this.params = params;
    }

    // Getters and setters
    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public Object[] getParams() { return params; }
    public void setParams(Object[] params) { this.params = params; }
}
