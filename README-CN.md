# FlashRPC

FlashRPC 是一个基于 Netty 的轻量级 RPC (远程过程调用) 框架。它提供了简单易用的 API 来实现分布式系统中的服务调用。

## 特性

- 基于 Netty 的高性能网络传输
- 支持 JSON 序列化
- 简单的服务注册和发现机制
- 易于使用的客户端 API

## 项目结构

- flashrpc-common: 公共模块,包含共享的接口和数据结构
- flashrpc-transport: 网络传输模块,基于 Netty 实现
- flashrpc-client: 客户端模块,提供服务调用 API
- flashrpc-example: 示例模块,展示如何使用 FlashRPC

## 快速开始

### 服务端

1. 定义服务接口:
```java
public interface CalculatorService {
    int add(int a, int b);
    int subtract(int a, int b);
    int multiply(int a, int b);
    int divide(int a, int b);
}
```

2. 实现服务接口:
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
        return a*b;
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

3. 创建服务提供者:
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

4. 启动服务器:
```java
public class ExampleServer {
    public static void main(String[] args) throws Exception {
        FlashRpcServer server = new FlashRpcServer(8080);
        server.start();
    }
}
```

### 客户端

1. 创建 RPC 客户端:
```java
FlashRpcClient client = new FlashRpcClientBuilder()
    .host("localhost")
    .port(8080)
    .build();
```

2. 调用远程服务:
```java
public static void main(String[] args) {
    CalculatorService calculatorService = client.create(CalculatorService.class);
    int result = calculatorService.add(5, 3);
    System.out.println("5 + 3 = " + result);
    result = calculatorService.subtract(10, 4);
    System.out.println("10 - 4 = " + result);
    result = calculatorService.multiply(6, 7);
    System.out.println("6 7 = " + result);
    result = calculatorService.divide(20, 5);
    System.out.println("20 / 5 = " + result);
}
```

## 构建和运行

使用 Maven 构建项目:
```shell
mvn clean package
```

运行示例服务器:
```shell
java -cp flashrpc-example/target/flashrpc-example-1.0-SNAPSHOT.jar org.example.flashrpc.example.server.ExampleServer
```


运行示例客户端:
```shell
java -cp flashrpc-example/target/flashrpc-example-1.0-SNAPSHOT.jar org.example.flashrpc.example.client.ExampleClient
```


## 配置

FlashRPC 使用 logback 进行日志记录。您可以通过修改 `flashrpc-transport/src/main/resources/logback.xml` 文件来自定义日志配置。

## 扩展

FlashRPC 使用 Java SPI 机制来加载服务实现。要添加新的服务,只需:

1. 实现 `RpcServiceProvider` 接口
2. 在 `META-INF/services/org.example.flashrpc.common.rpc.spi.RpcServiceProvider` 文件中添加您的实现类的全限定名

## 贡献

欢迎提交问题和拉取请求。对于重大更改,请先开issue讨论您想要更改的内容。

## 许可证
本项目采用 MIT 许可证。详情请参阅 [LICENSE](LICENSE) 文件。
