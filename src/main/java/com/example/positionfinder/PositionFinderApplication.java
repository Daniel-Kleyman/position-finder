package com.example.positionfinder;

import com.example.positionfinder.service.PositionFinderService;
import com.example.positionfinder.service.TestMap;
import com.example.positionfinder.service.TestWriteToExcel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;


@SpringBootApplication
@EnableScheduling
public class PositionFinderApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(PositionFinderApplication.class, args);

        // Get PositionFinderService bean from the application context
        PositionFinderService positionFinderService = context.getBean(PositionFinderService.class);

        // Optionally call getResults() if needed
       // positionFinderService.getResults();
    }
}