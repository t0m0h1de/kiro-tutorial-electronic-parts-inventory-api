package com.example.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ElectronicPartsInventoryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectronicPartsInventoryApiApplication.class, args);
    }

}