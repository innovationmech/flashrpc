package org.innovationmech.flashrpc.common.rpc;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse {
    private Object result;
    private Throwable exception;

    public boolean hasException() {
        return exception != null;
    }
}
