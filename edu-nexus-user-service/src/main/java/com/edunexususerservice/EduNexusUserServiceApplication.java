package com.edunexususerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.edunexususerservice", "com.edunexususerservice.domain.user.config"})
@EnableDiscoveryClient
public class EduNexusUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduNexusUserServiceApplication.class, args);
	}

}
