package com.sky.takeout.common;

import com.sky.takeout.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CatalogMissCacheTest {

    private final ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

    @Test
    void shouldRememberFailureUntilCacheIsCleared() {
        CatalogMissCache missCache = new CatalogMissCache(providerOf(cacheManager));

        assertNull(missCache.findMissed(CatalogMissCache.DISH_CATEGORY_SCOPE, 99L));

        missCache.recordMissed(
                CatalogMissCache.DISH_CATEGORY_SCOPE,
                99L,
                new BusinessException(404, "Category not found")
        );

        BusinessException cached = missCache.findMissed(
                CatalogMissCache.DISH_CATEGORY_SCOPE,
                99L
        );
        assertNotNull(cached);
        assertEquals(404, cached.getCode());
        assertEquals("Category not found", cached.getMessage());

        assertNull(missCache.findMissed(CatalogMissCache.SETMEAL_CATEGORY_SCOPE, 99L));

        cacheManager.getCache(CacheNames.CATALOG_MISS).clear();
        assertNull(missCache.findMissed(CatalogMissCache.DISH_CATEGORY_SCOPE, 99L));
    }

    @Test
    void shouldDegradeWhenNoCacheManagerIsAvailable() {
        CatalogMissCache missCache = new CatalogMissCache(new ObjectProvider<>() {
            @Override
            public CacheManager getIfAvailable() {
                return null;
            }
        });

        assertNull(missCache.findMissed(CatalogMissCache.DISH_CATEGORY_SCOPE, 1L));
        assertNotNull(missCache.recordMissed(
                CatalogMissCache.DISH_CATEGORY_SCOPE,
                1L,
                new BusinessException(404, "Category not found")
        ));
    }

    private ObjectProvider<CacheManager> providerOf(CacheManager cacheManager) {
        return new ObjectProvider<>() {
            @Override
            public CacheManager getIfAvailable() {
                return cacheManager;
            }
        };
    }
}
