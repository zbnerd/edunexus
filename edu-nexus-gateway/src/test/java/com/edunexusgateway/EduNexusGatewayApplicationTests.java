package com.edunexusgateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.cloud.discovery.enabled=false",
	"eureka.client.enabled=false",
	"spring.main.web-application-type=reactive"
})
class EduNexusGatewayApplicationTests {

    @Test
    void contextLoads() {
    }

}
