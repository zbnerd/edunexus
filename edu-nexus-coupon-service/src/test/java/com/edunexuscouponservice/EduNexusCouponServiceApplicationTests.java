package com.edunexuscouponservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.kafka.enabled=false",
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class EduNexusCouponServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
