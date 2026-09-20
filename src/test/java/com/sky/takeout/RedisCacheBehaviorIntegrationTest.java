package com.sky.takeout;

import com.sky.takeout.common.CacheNames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "sky.cache.enabled=true")
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(
        named = "REDIS_INTEGRATION_TEST",
        matches = "true"
)
class RedisCacheBehaviorIntegrationTest {

    private static final int THREADS = 8;
    private static final long MAX_TTL_MILLIS = 660_000L;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @AfterEach
    void clearCaches() {
        List.of(CacheNames.DISHES, CacheNames.CATALOG_MISS).forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test
    void shouldLoadColdKeyOnlyOnceWhenThreadsRace() throws Exception {
        Cache cache = cacheManager.getCache(CacheNames.DISHES);
        AtomicInteger loads = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int index = 0; index < THREADS; index++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return cache.get("stampede", () -> {
                        loads.incrementAndGet();
                        Thread.sleep(100);
                        return "loaded";
                    });
                }));
            }

            start.countDown();
            for (Future<?> future : futures) {
                assertNotNull(future.get());
            }
        } finally {
            pool.shutdown();
        }

        assertEquals(1, loads.get());
    }

    @Test
    void shouldSpreadExpiryTimesAcrossEntries() {
        Cache cache = cacheManager.getCache(CacheNames.DISHES);
        assertNotNull(cache);

        Set<Long> observedTtls = new HashSet<>();
        for (int index = 0; index < 20; index++) {
            cache.put(index, "value-" + index);

            Long ttl = stringRedisTemplate.getExpire(
                    "sky:cache:" + CacheNames.DISHES + ":" + index,
                    TimeUnit.MILLISECONDS
            );
            assertNotNull(ttl);
            assertTrue(ttl > 0 && ttl <= MAX_TTL_MILLIS, "ttl=" + ttl);
            observedTtls.add(ttl);
        }

        assertTrue(
                observedTtls.size() > 1,
                "all entries shared the same expiry: " + observedTtls
        );
    }
}
