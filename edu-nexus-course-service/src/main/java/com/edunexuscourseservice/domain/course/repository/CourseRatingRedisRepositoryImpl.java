package com.edunexuscourseservice.domain.course.repository;

import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import com.edunexuscourseservice.domain.course.util.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CourseRatingRedisRepositoryImpl implements CourseRatingRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveReviewRating(Long courseId, int rating) {
        log.info("Saving review rating for course id {}", courseId);

        String totalKey = generateRedisKeyRatingTotal(courseId);
        String countKey = generateRedisKeyRatingCount(courseId);

        if (redisTemplate.opsForValue().get(totalKey) == null) {
            log.info("initializing review rating total for course id {}", courseId);
            redisTemplate.opsForValue().set(totalKey, "0");
        }

        if (redisTemplate.opsForValue().get(countKey) == null) {
            log.info("initializing review rating count for course id {}", courseId);
            redisTemplate.opsForValue().set(countKey, "0");
        }

        redisTemplate.opsForValue().increment(totalKey, rating);
        redisTemplate.opsForValue().increment(countKey);
    }

    @Override
    public void updateReviewRating(Long courseId, int originalRating, int updatedRating) {

        log.info("Updating review rating for course id {}", courseId);

        String totalKey = generateRedisKeyRatingTotal(courseId);

        redisTemplate.opsForValue().decrement(totalKey, originalRating);
        redisTemplate.opsForValue().increment(totalKey, updatedRating);

    }

    @Override
    public void deleteReviewRating(Long courseId, int originalRating) {
        log.info("Deleting review rating for course id {}", courseId);

        String totalKey = generateRedisKeyRatingTotal(courseId);
        String countKey = generateRedisKeyRatingCount(courseId);

        redisTemplate.opsForValue().decrement(totalKey, originalRating);
        redisTemplate.opsForValue().decrement(countKey);
    }

    @Override
    public double getAverageReviewRating(Long courseId) {
        log.info("Getting average review rating for course id {}", courseId);
        String total = String.valueOf(redisTemplate.opsForValue().get(generateRedisKeyRatingTotal(courseId)));
        String count = String.valueOf(redisTemplate.opsForValue().get(generateRedisKeyRatingCount(courseId)));

        if (total == null || count == null) {
            throw new NotFoundException("Course Not Found " + courseId);
        }

        if (Integer.parseInt(count) == 0) {
            return 0.0;
        }

        return Integer.parseInt(total) / 1.0 / Integer.parseInt(count);
    }

    private String generateRedisKeyRatingTotal(Long courseId) {
        return RedisKey.COURSE_RATING_TOTAL.getKey(courseId);
    }

    private String generateRedisKeyRatingCount(Long courseId) {
        return RedisKey.COURSE_RATING_COUNT.getKey(courseId);
    }

}
