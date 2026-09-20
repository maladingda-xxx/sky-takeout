package com.sky.takeout.config;

import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class JitterRedisCacheWriter implements RedisCacheWriter {

    private final RedisCacheWriter delegate;
    private final TtlJitter ttlJitter;

    public JitterRedisCacheWriter(RedisCacheWriter delegate, TtlJitter ttlJitter) {
        this.delegate = delegate;
        this.ttlJitter = ttlJitter;
    }

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        delegate.put(name, key, value, ttlJitter.apply(ttl));
    }

    @Override
    public byte[] get(String name, byte[] key) {
        return delegate.get(name, key);
    }

    @Override
    public byte[] get(
            String name,
            byte[] key,
            Supplier<byte[]> valueLoader,
            Duration ttl,
            boolean updateCache
    ) {
        return delegate.get(name, key, valueLoader, ttlJitter.apply(ttl), updateCache);
    }

    @Override
    public CompletableFuture<byte[]> retrieve(String name, byte[] key, Duration ttl) {
        return delegate.retrieve(name, key, ttl);
    }

    @Override
    public CompletableFuture<Void> store(String name, byte[] key, byte[] value, Duration ttl) {
        return delegate.store(name, key, value, ttlJitter.apply(ttl));
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
        return delegate.putIfAbsent(name, key, value, ttlJitter.apply(ttl));
    }

    @Override
    public void remove(String name, byte[] key) {
        delegate.remove(name, key);
    }

    @Override
    public void clean(String name, byte[] pattern) {
        delegate.clean(name, pattern);
    }

    @Override
    public void clearStatistics(String name) {
        delegate.clearStatistics(name);
    }

    @Override
    public CacheStatistics getCacheStatistics(String cacheName) {
        return delegate.getCacheStatistics(cacheName);
    }

    @Override
    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector collector) {
        return new JitterRedisCacheWriter(
                delegate.withStatisticsCollector(collector),
                ttlJitter
        );
    }

}
