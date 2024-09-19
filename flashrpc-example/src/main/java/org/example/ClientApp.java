package org.example;

import org.example.transport.client.FlashRpcClient;
import org.example.transport.service.CalculatorService;

public class ClientApp {

    public static void main(String[] args) {
        try {
            FlashRpcClient client = new FlashRpcClient("localhost", 8080);
            client.start();

            CalculatorService calculatorService = client.create(CalculatorService.class);

            System.out.println("10 + 5 = " + calculatorService.add(10, 5));
            System.out.println("10 - 5 = " + calculatorService.subtract(10, 5));
            System.out.println("10 * 5 = " + calculatorService.multiply(10, 5));
            System.out.println("10 / 5 = " + calculatorService.divide(10, 5));

            client.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
