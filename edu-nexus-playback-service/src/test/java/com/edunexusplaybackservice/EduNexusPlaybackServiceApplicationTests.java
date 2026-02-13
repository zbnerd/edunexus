package com.edunexusplaybackservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.cloud.discovery.enabled=false",
	"eureka.client.enabled=false",
	"spring.kafka.enabled=false",
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
	"spring.sql.init.mode=never"
})
class EduNexusPlaybackServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
