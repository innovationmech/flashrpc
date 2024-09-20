# FlashRPC

FlashRPC is a lightweight RPC (Remote Procedure Call) framework based on Netty. It provides a simple and user-friendly API for service calls in distributed systems.

## Features

- High-performance network transmission based on Netty
- Supports JSON serialization
- Simple service registration and discovery mechanism
- Easy-to-use client API

## Project Structure

- flashrpc-common: Common module containing shared interfaces and data structures
- flashrpc-transport: Network transport module, implemented with Netty
- flashrpc-client: Client module providing the service call API
- flashrpc-example: Example module demonstrating how to use FlashRPC

## Quick Start

### Server

1. Define the service interface:
```java
public interface CalculatorService {
    int add(int a, int b);
    int subtract(int a, int b);
    int multiply(int a, int b);
    int divide(int a, int b);
}
```

2. Implement the service interface:
```java
public class CalculatorServiceImpl implements CalculatorService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int subtract(int a, int b) {
        return a - b;
    }

    @Override
    public int multiply(int a, int b) {
        return a * b;
    }

    @Override
    public int divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Divisor cannot be zero");
        }
        return a / b;
    }
}
```

3. Create a service provider:
```java
public class CalculatorServiceProvider implements RpcServiceProvider {
    @Override
    public String getServiceName() {
        return CalculatorService.class.getName();
    }

    @Override
    public Class<?> getServiceInterface() {
        return CalculatorService.class;
    }

    @Override
    public Object getServiceImpl() {
        return new CalculatorServiceImpl();
    }
}
```

4. Start the server:
```java
public class ExampleServer {
    public static void main(String[] args) throws Exception {
        FlashRpcServer server = new FlashRpcServer(8080);
        server.start();
    }
}
```

### Client

1. Create an RPC client:
```java
FlashRpcClient client = new FlashRpcClientBuilder()
    .host("localhost")
    .port(8080)
    .build();
```

2. Call the remote service:
```java
public static void main(String[] args) {
    CalculatorService calculatorService = client.create(CalculatorService.class);
    int result = calculatorService.add(5, 3);
    System.out.println("5 + 3 = " + result);
    result = calculatorService.subtract(10, 4);
    System.out.println("10 - 4 = " + result);
    result = calculatorService.multiply(6, 7);
    System.out.println("6 * 7 = " + result);
    result = calculatorService.divide(20, 5);
    System.out.println("20 / 5 = " + result);
}
```

## Build and Run

Build the project using Maven:
```shell
mvn clean package
```

Run the example server:
```shell
java -cp flashrpc-example/target/flashrpc-example-1.0-SNAPSHOT.jar org.example.flashrpc.example.server.ExampleServer
```

Run the example client:
```shell
java -cp flashrpc-example/target/flashrpc-example-1.0-SNAPSHOT.jar org.example.flashrpc.example.client.ExampleClient
```

## Configuration

FlashRPC uses Logback for logging. You can customize the logging configuration by modifying the `flashrpc-transport/src/main/resources/logback.xml` file.

## Extension

FlashRPC uses Java SPI (Service Provider Interface) to load service implementations. To add a new service:

1. Implement the `RpcServiceProvider` interface
2. Add the fully qualified name of your implementation class to the `META-INF/services/org.example.flashrpc.common.rpc.spi.RpcServiceProvider` file

## Contribution

We welcome issue submissions and pull requests. For significant changes, please open an issue to discuss your proposed changes first.

## License

This project is licensed under the MIT License. For details, please refer to the [LICENSE](LICENSE) file.