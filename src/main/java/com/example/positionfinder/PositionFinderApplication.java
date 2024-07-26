package com.example.positionfinder;

import com.example.positionfinder.service.PositionFinderService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PositionFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PositionFinderApplication.class, args);
        PositionFinderService positionFinderService = new PositionFinderService();
        positionFinderService.getResults();

    }
}
