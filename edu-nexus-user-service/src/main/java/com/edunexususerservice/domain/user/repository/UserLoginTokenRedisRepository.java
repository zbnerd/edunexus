package com.edunexususerservice.domain.user.repository;

public interface UserLoginTokenRedisRepository {
    void saveLoginToken(Long userId, String token, long ttlInSeconds);
    String findLoginToken(Long userId);
    void deleteLoginToken(Long userId);
}
