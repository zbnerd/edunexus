package com.edunexusattendanceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {
        "com.edunexusattendanceservice",
        "com.edunexusobservability"
})
public class EduNexusAttendanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduNexusAttendanceServiceApplication.class, args);
    }

}
