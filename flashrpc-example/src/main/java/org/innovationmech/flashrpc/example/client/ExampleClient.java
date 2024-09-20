package org.innovationmech.flashrpc.example.client;

import org.innovationmech.flashrpc.client.FlashRpcClient;
import org.innovationmech.flashrpc.client.FlashRpcClientBuilder;
import org.innovationmech.flashrpc.example.common.service.CalculatorService;

public class ExampleClient {

    public static void main(String[] args) throws Exception {
        FlashRpcClient client = new FlashRpcClientBuilder()
                .host("localhost")
                .port(8080)
                .build();

        try {
            CalculatorService calculatorService = client.create(CalculatorService.class);
            int result = calculatorService.add(5, 3);
            System.out.println("5 + 3 = " + result);

            result = calculatorService.subtract(10, 4);
            System.out.println("10 - 4 = " + result);

            result = calculatorService.multiply(6, 7);
            System.out.println("6 * 7 = " + result);

            result = calculatorService.divide(20, 5);
            System.out.println("20 / 5 = " + result);
        } finally {
            client.stop();
        }
    }
}
