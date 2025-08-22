package com.careercoach.careercoachapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CareerCoachApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareerCoachApiApplication.class, args);
    }

}

