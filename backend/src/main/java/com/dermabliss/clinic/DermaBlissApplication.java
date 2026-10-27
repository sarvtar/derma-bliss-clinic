package com.dermabliss.clinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DermaBlissApplication {

    public static void main(String[] args) {
        // Enforce clinic standard timezone
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(DermaBlissApplication.class, args);
    }
}
