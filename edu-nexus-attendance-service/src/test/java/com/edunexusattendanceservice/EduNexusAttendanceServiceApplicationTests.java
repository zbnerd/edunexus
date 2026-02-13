package com.edunexusattendanceservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
class EduNexusAttendanceServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
