package com.edunexuscourseservice.domain.course.config.init;

import com.edunexuscourseservice.domain.course.aop.annotation.LogExecutionTime;
import com.edunexuscourseservice.domain.course.service.CourseRatingService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisInit {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CourseRatingService courseRatingService;

    @PostConstruct
    public void init() {

        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }

        courseRatingService.initCourseRatings();

    }
}
