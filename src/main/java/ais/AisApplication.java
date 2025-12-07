package ais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application Class for Academic Information System
 * Entry point for the Spring Boot application
 */
@SpringBootApplication
public class AisApplication {

    public static void main(String[] args) {
        SpringApplication.run(AisApplication.class, args);

        System.out.println("\n===========================================");
        System.out.println("Academic Information System Started!");
        System.out.println("===========================================");
        System.out.println("Access the application at: http://localhost:8080");
        System.out.println("\nDefault admin credentials:");
        System.out.println("  Username: Admin");
        System.out.println("  Password: Admin");
        System.out.println("===========================================\n");
    }
}