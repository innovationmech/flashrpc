package org.innovationmech.flashrpc.example.client;

import org.innovationmech.flashrpc.client.FlashRpcClient;
import org.innovationmech.flashrpc.client.FlashRpcClientBuilder;
import org.innovationmech.flashrpc.example.common.service.CalculatorService;
import org.innovationmech.flashrpc.example.common.service.GreetService;
import org.innovationmech.flashrpc.example.common.service.UserService;
import org.innovationmech.flashrpc.example.proto.GreetProto.GreetResponse;
import org.innovationmech.flashrpc.example.proto.GreetProto.GreetRequest;
import org.innovationmech.flashrpc.example.proto.UserProto.GetUserRequest;
import org.innovationmech.flashrpc.example.proto.UserProto.GetUserResponse;
import org.innovationmech.flashrpc.example.proto.UserProto.CreateUserRequest;
import org.innovationmech.flashrpc.example.proto.UserProto.CreateUserResponse;

public class ExampleClient {

    public static void main(String[] args) throws Exception {
        FlashRpcClient client = new FlashRpcClientBuilder()
                .host("localhost")
                .port(8080)
                .build();

        try {
            // CalculatorService 调用
            CalculatorService calculatorService = client.create(CalculatorService.class);
            int result = calculatorService.add(5, 3);
            System.out.println("5 + 3 = " + result);

            result = calculatorService.subtract(10, 4);
            System.out.println("10 - 4 = " + result);

            result = calculatorService.multiply(6, 7);
            System.out.println("6 * 7 = " + result);

            result = calculatorService.divide(20, 5);
            System.out.println("20 / 5 = " + result);

            // UserService 调用
            UserService userService = client.create(UserService.class);

            // 获取用户
            GetUserRequest getUserRequest = GetUserRequest.newBuilder()
                .setUserId(1)
                .build();
            GetUserResponse getUserResponse = userService.getUser(getUserRequest);
            System.out.println("获取到的用户: " + getUserResponse.getUser().getName());

            // 创建用户
            CreateUserRequest createUserRequest = CreateUserRequest.newBuilder()
                .setName("张三")
                .setEmail("zhangsan@example.com")
                .build();
            CreateUserResponse createUserResponse = userService.createUser(createUserRequest);
            System.out.println("创建的用户ID: " + createUserResponse.getUser().getId());

            // GreetService 调用
            GreetService greetService = client.create(GreetService.class);
            GreetResponse greetResponse = greetService.greet(GreetRequest.newBuilder().setMsg("FlashRPC").build());
            System.out.println("Greet: " + greetResponse.getMsg());
        } finally {
            client.stop();
        }
    }
}
