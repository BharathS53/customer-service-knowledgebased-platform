package com.example.customerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Entry Point for the Customer Service Knowledge-Based Resolution Platform.
 * Bootstraps Spring Boot, initializes Hibernate ORM, and starts the embedded Tomcat server.
 */
@SpringBootApplication
public class CustomerServiceKbApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceKbApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" Customer Service Knowledge-Based Resolution Platform Started!   ");
        System.out.println(" Server Port: 8081                                                ");
        System.out.println(" Web Login Portal:     http://localhost:8081/login                ");
        System.out.println(" Platform Home:        http://localhost:8081                      ");
        System.out.println(" Health API Endpoint:  http://localhost:8081/api/health           ");
        System.out.println("==================================================================");
    }
}