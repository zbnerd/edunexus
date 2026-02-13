package com.edunexuscourseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@ComponentScan(basePackages = {
	"com.edunexuscourseservice",
	"com.edunexusobservability"
})
public class EduNexusCourseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduNexusCourseServiceApplication.class, args);
	}

}
