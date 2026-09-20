package com.sky.takeout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(
        named = "REDIS_INTEGRATION_TEST",
        matches = "true"
)
class RedisConnectionIntegrationTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void shouldReadWriteAndDeleteRedisValue() {
        String key = "sky:test:" + UUID.randomUUID();

        redisTemplate.opsForValue().set(
                key,
                "connected",
                Duration.ofSeconds(30)
        );

        assertEquals(
                "connected",
                redisTemplate.opsForValue().get(key)
        );
        assertEquals(Boolean.TRUE, redisTemplate.delete(key));
    }
}
