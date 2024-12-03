package com.edunexuscourseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EduNexusCourseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduNexusCourseServiceApplication.class, args);
	}

}
