package com.aiproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiConsumptionProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiConsumptionProxyApplication.class, args);
    }
}
