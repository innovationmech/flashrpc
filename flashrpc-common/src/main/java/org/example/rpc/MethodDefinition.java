package org.example.rpc;

import java.util.List;

import lombok.Data;

@Data
public class MethodDefinition {
    private String name;
    private List<String> parameterTypes;
    private String returnType;
}
