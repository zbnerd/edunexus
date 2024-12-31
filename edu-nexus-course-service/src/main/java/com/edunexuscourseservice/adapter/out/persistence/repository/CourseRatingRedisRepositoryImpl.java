package com.edunexuscourseservice.adapter.out.persistence.repository;

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
    public void cacheReviewRating(Long courseId, int rating) {

        String totalKey = generateRedisKeyRatingTotal(courseId);
        String countKey = generateRedisKeyRatingCount(courseId);

        if (redisTemplate.opsForValue().get(totalKey) == null) {
            redisTemplate.opsForValue().set(totalKey, 0);
        }

        if (redisTemplate.opsForValue().get(countKey) == null) {
            redisTemplate.opsForValue().set(countKey, 0);
        }

        redisTemplate.opsForValue().increment(totalKey, rating);
        redisTemplate.opsForValue().increment(countKey);
    }

    @Override
    public void updateReviewRating(Long courseId, int originalRating, int updatedRating) {


        String totalKey = generateRedisKeyRatingTotal(courseId);

        redisTemplate.opsForValue().decrement(totalKey, originalRating);
        redisTemplate.opsForValue().increment(totalKey, updatedRating);

    }

    @Override
    public void deleteReviewRating(Long courseId, int originalRating) {

        String totalKey = generateRedisKeyRatingTotal(courseId);
        String countKey = generateRedisKeyRatingCount(courseId);

        redisTemplate.opsForValue().decrement(totalKey, originalRating);
        redisTemplate.opsForValue().decrement(countKey);
    }

    @Override
    public double getAverageReviewRating(Long courseId) {
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

    @Override
    public void initializeRating(Long courseId, int total, int count) {
        String totalKey = generateRedisKeyRatingTotal(courseId);
        String countKey = generateRedisKeyRatingCount(courseId);

        redisTemplate.opsForValue().set(totalKey, total);
        redisTemplate.opsForValue().set(countKey, count);
    }

    private String generateRedisKeyRatingTotal(Long courseId) {
        return RedisKey.COURSE_RATING_TOTAL.getKey(courseId);
    }

    private String generateRedisKeyRatingCount(Long courseId) {
        return RedisKey.COURSE_RATING_COUNT.getKey(courseId);
    }

}
