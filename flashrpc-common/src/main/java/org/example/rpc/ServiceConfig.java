package org.example.rpc;

import lombok.Data;

import java.util.List;

@Data
public class ServiceConfig {
   private List<ServiceDefinition> services; 
}
