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

        Set<String> keys = redisTemplate.keys("edu-nexus-course:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }

        List<Course> courseList = courseRepository.findAll();
        log.info("course List count {}", courseList.size());

        for (Course course : courseList) {
            List<CourseRating> ratingList = courseRatingRepository.findByCourseId(course.getId());
            List<Integer> ratings = ratingList.stream()
                    .map(CourseRating::getRating)
                    .toList();

            int totalRating = ratings.stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            courseRatingRedisRepository.initializeRating(course.getId(), totalRating, ratingList.size());
        }
    }
}
