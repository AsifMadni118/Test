package com.proteinpro.protein;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ProteinServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProteinServiceApplication.class, args);
    }
}
