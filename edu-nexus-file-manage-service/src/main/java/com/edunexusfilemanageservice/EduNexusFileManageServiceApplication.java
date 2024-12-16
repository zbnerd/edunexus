package com.edunexusfilemanageservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EduNexusFileManageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduNexusFileManageServiceApplication.class, args);
	}

}
