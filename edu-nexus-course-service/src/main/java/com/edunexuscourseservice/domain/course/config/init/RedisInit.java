package com.edunexuscourseservice.domain.course.config.init;

import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.entity.CourseRating;
import com.edunexuscourseservice.domain.course.repository.CourseRatingRedisRepository;
import com.edunexuscourseservice.domain.course.repository.CourseRatingRepository;
import com.edunexuscourseservice.domain.course.repository.CourseRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisInit {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CourseRepository courseRepository;
    private final CourseRatingRepository courseRatingRepository;
    private final CourseRatingRedisRepository courseRatingRedisRepository;

    @PostConstruct
    public void init() {

        log.info("[Redis Init] Deleting all existing Redis key");
        Set<String> keys = redisTemplate.keys("edu-nexus-course:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }

        log.info("[Redis Init] Loading all courses");
        List<Course> courseList = courseRepository.findAll();

        for (Course course : courseList) {
            log.info("[Redis Init] Loading all course ratings for courseId: {}", course.getId());
            List<CourseRating> courseRatingList = courseRatingRepository.findByCourseId(course.getId());
            for (CourseRating courseRating : courseRatingList) {
                courseRatingRedisRepository.saveReviewRating(course.getId(), courseRating.getRating());
            }
        }
    }
}
