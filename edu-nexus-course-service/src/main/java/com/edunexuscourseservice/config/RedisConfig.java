package com.edunexuscourseservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("edu-nexus-redis");
        config.setPort(6379);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Key Serializer - String for readable keys
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Value Serializer - JSON for proper object serialization
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Enable transaction support for multi-operation atomicity
        redisTemplate.setEnableTransactionSupport(true);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                // Use JSON serialization for cache values
                .serializeValuesWith(
                    org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                // Don't cache null values - treat as miss
                .disableCachingNullValues()
                // Add cache key prefix
                .prefixCacheNameWith("edunexus:");

        // Cache-specific TTL configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Courses cache: 10 minutes TTL - course data changes infrequently
        cacheConfigurations.put("courses", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Course ratings cache: 2 minutes TTL - ratings change more frequently
        cacheConfigurations.put("courseRatings", defaultConfig.entryTtl(Duration.ofMinutes(2)));

        // Course sessions cache: 10 minutes TTL - session data changes infrequently
        cacheConfigurations.put("courseSessions", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Default cache: 5 minutes TTL as per ADR-000 (Cache-Aside pattern)
        RedisCacheConfiguration defaultConfigWithTtl = defaultConfig.entryTtl(Duration.ofMinutes(5));

        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
                .cacheDefaults(defaultConfigWithTtl)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

}
