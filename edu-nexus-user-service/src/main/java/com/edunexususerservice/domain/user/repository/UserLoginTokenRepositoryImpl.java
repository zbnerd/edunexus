package com.edunexususerservice.domain.user.repository;

import com.edunexususerservice.domain.user.util.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserLoginTokenRepositoryImpl implements UserLoginTokenRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void saveLoginToken(Long userId, String token, long ttlInSeconds) {
        String redisKey = generateRedisKey(userId);
        redisTemplate.opsForValue().set(redisKey, token, ttlInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String findLoginToken(Long userId) {
        String redisKey = generateRedisKey(userId);
        return redisTemplate.opsForValue().get(redisKey);
    }

    @Override
    public void deleteLoginToken(Long userId) {
        String redisKey = generateRedisKey(userId);
        if (findLoginToken(userId) != null) {
            redisTemplate.delete(redisKey);
        }
    }

    private String generateRedisKey(Long userId) {
        return RedisKey.USER_LOGIN_TOKEN.getKey(String.valueOf(userId));
    }
}
