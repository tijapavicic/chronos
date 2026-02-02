package com.example.chronos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.example.chronos.config")
public class ChronosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChronosApplication.class, args);
    }

}
