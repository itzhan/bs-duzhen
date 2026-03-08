package com.carmaintenance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.carmaintenance.mapper")
public class CarMaintenanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarMaintenanceApplication.class, args);
    }
}
