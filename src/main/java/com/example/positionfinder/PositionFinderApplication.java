package com.example.positionfinder;

import com.example.positionfinder.service.PositionFinderService;
import com.example.positionfinder.service.TestMap;
import com.example.positionfinder.service.TestWriteToExcel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class PositionFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PositionFinderApplication.class, args);
        PositionFinderService positionFinderService = new PositionFinderService();
        positionFinderService.getResults();
    //    Map<String, String> testMap = TestMap.createMap();
      //  TestWriteToExcel.writeToExcel(testMap);

    }
}
