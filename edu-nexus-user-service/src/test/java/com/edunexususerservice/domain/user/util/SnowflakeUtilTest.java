package com.edunexususerservice.domain.user.util;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SnowflakeUtilTest {

    @Test
    void SnowflakeUtilTest() {
        System.out.println(SnowflakeUtil.generateId());
    }
}
