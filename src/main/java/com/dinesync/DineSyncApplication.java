package com.dinesync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DineSync Application Entry Point
 * This is the main class that starts the Spring Boot server.
 * Running this class starts an embedded Tomcat server on port 8080.
 */
@SpringBootApplication
public class DineSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(DineSyncApplication.class, args);
        System.out.println("✅ DineSync server started! Visit http://localhost:8080");
    }
}
