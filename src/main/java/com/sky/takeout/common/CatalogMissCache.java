package com.sky.takeout.common;

import com.sky.takeout.exception.BusinessException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CatalogMissCache {

    public static final String DISH_CATEGORY_SCOPE = "dish-category";
    public static final String SETMEAL_CATEGORY_SCOPE = "setmeal-category";
    public static final String SETMEAL_SCOPE = "setmeal";

    private final ObjectProvider<CacheManager> cacheManagerProvider;

    public CatalogMissCache(ObjectProvider<CacheManager> cacheManagerProvider) {
        this.cacheManagerProvider = cacheManagerProvider;
    }

    public BusinessException findMissed(String scope, Object key) {
        Cache cache = cache();
        if (cache == null) {
            return null;
        }

        Cache.ValueWrapper wrapper = cache.get(cacheKey(scope, key));
        if (wrapper == null || !(wrapper.get() instanceof CachedFailure failure)) {
            return null;
        }

        return new BusinessException(failure.code(), failure.message());
    }

    public BusinessException recordMissed(String scope, Object key, BusinessException exception) {
        Cache cache = cache();
        if (cache != null) {
            cache.put(
                    cacheKey(scope, key),
                    new CachedFailure(exception.getCode(), exception.getMessage())
            );
        }

        return exception;
    }

    private Cache cache() {
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        return cacheManager == null ? null : cacheManager.getCache(CacheNames.CATALOG_MISS);
    }

    private String cacheKey(String scope, Object key) {
        return scope + ":" + key;
    }
}
