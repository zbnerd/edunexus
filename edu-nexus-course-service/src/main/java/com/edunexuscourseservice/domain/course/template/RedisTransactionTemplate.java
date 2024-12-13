package com.edunexuscourseservice.domain.course.template;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class RedisTransactionTemplate<K, V> {

    private final RedisTemplate<K, V> redisTemplate;

    // 람다를 받아 트랜잭션 실행
    public void execute(Consumer<RedisTemplate<K, V>> action) {
        redisTemplate.multi(); // 트랜잭션 시작
        try {
            action.accept(redisTemplate); // 트랜잭션 내에서 작업 실행
            redisTemplate.exec(); // 트랜잭션 커밋
        } catch (Exception e) {
            redisTemplate.discard(); // 트랜잭션 롤백
            throw e;
        }
    }
}
