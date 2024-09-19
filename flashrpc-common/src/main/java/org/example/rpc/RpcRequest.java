package org.example.rpc;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest {
    private String interfaceName;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[] params;
}
