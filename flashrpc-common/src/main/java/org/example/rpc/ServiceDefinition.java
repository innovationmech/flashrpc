package org.example.rpc;

import java.util.List;

import lombok.Data;

@Data
public class ServiceDefinition {
    private String interfaceName;
    private String implementationClass;
    private List<MethodDefinition> methods;
}
