package com.edunexusgraphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EduNexusGraphqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduNexusGraphqlApplication.class, args);
	}

}
