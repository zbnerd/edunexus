package com.edunexuscourseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {
	"com.edunexuscourseservice",
	"com.edunexusobservability"
})
public class EduNexusCourseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduNexusCourseServiceApplication.class, args);
	}

}
