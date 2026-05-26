package com.verodata.retail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.verodata.retail") // <-- AGREGA ESTA LÍNEA AQUÍ
public class VerodataBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VerodataBackendApplication.class, args);
    }
}