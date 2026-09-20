package com.sky.takeout.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@ConditionalOnProperty(
        name = "sky.cache.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            CacheProperties cacheProperties,
            ObjectMapper objectMapper
    ) {
        GenericJackson2JsonRedisSerializer valueSerializer =
                GenericJackson2JsonRedisSerializer.builder()
                        .objectMapper(objectMapper.copy())
                        .defaultTyping(true)
                        .typeHintPropertyName("@class")
                        .build();
        RedisCacheConfiguration defaults = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(cacheProperties.getTtl())
                .disableCachingNullValues()
                .computePrefixWith(cacheName ->
                        cacheProperties.getKeyPrefix() + cacheName + ":"
                )
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(valueSerializer)
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .build();
    }
}
