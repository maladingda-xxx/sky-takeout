package com.sky.takeout;

import com.sky.takeout.common.CacheNames;
import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.SetmealCreateDTO;
import com.sky.takeout.dto.SetmealDishDTO;
import com.sky.takeout.service.CategoryService;
import com.sky.takeout.service.DishService;
import com.sky.takeout.service.SetmealService;
import com.sky.takeout.service.UserCatalogService;
import com.sky.takeout.vo.DishUserVO;
import com.sky.takeout.vo.SetmealDetailVO;
import com.sky.takeout.vo.SetmealPageVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@EnabledIfEnvironmentVariable(
        named = "REDIS_INTEGRATION_TEST",
        matches = "true"
)
class CatalogCacheIntegrationTest {

    @Autowired
    private UserCatalogService userCatalogService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCachesBeforeTest() {
        clearCatalogCaches();
    }

    @AfterEach
    void clearCaches() {
        clearCatalogCaches();
    }

    private void clearCatalogCaches() {
        List.of(
                CacheNames.CATEGORIES,
                CacheNames.DISHES,
                CacheNames.SETMEALS,
                CacheNames.SETMEAL_DETAIL
        ).forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test
    void shouldCacheDishListAndEvictOnDishWrite() {
        Long dishId = createDish("Cached Dish");

        List<DishUserVO> firstRead = userCatalogService.listDishes(1L);
        assertTrue(firstRead.stream().anyMatch(dish -> dish.getId().equals(dishId)));

        Cache cache = cacheManager.getCache(CacheNames.DISHES);
        assertNotNull(cache);
        assertNotNull(cache.get(1L));

        List<DishUserVO> secondRead = userCatalogService.listDishes(1L);
        assertTrue(secondRead.stream().anyMatch(dish -> dish.getId().equals(dishId)));

        dishService.updateStatus(dishId, 0);
        assertNull(cache.get(1L));
        assertTrue(userCatalogService.listDishes(1L).stream()
                .noneMatch(dish -> dish.getId().equals(dishId)));
    }

    @Test
    void shouldCacheSetmealListAndDetailAndEvictOnSetmealWrite() {
        Long dishId = createDish("Setmeal Cached Dish");
        Long setmealId = createSetmeal(dishId);

        List<SetmealPageVO> firstRead = userCatalogService.listSetmeals(2L);
        assertTrue(firstRead.stream().anyMatch(item -> item.getId().equals(setmealId)));

        SetmealDetailVO detail = userCatalogService.getSetmealDetail(setmealId);
        assertTrue(detail.getDishes().stream()
                .anyMatch(item -> item.getDishId().equals(dishId)));

        Cache listCache = cacheManager.getCache(CacheNames.SETMEALS);
        Cache detailCache = cacheManager.getCache(CacheNames.SETMEAL_DETAIL);
        assertNotNull(listCache);
        assertNotNull(detailCache);
        assertNotNull(listCache.get(2L));
        assertNotNull(detailCache.get(setmealId));

        setmealService.updateStatus(setmealId, 0);
        assertNull(listCache.get(2L));
        assertNull(detailCache.get(setmealId));
    }

    @Test
    void shouldEvictCategoryCacheOnCategoryWrite() {
        userCatalogService.listCategories(1);

        Cache cache = cacheManager.getCache(CacheNames.CATEGORIES);
        assertNotNull(cache);
        assertNotNull(cache.get(1));

        categoryService.updateStatus(1L, 1);
        assertNull(cache.get(1));
    }

    private Long createDish(String name) {
        DishCreateDTO dto = new DishCreateDTO();
        dto.setName(name);
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("18.00"));
        dto.setStatus(1);
        return dishService.create(dto);
    }

    private Long createSetmeal(Long dishId) {
        SetmealDishDTO dishDTO = new SetmealDishDTO();
        dishDTO.setDishId(dishId);
        dishDTO.setCopies(1);

        SetmealCreateDTO dto = new SetmealCreateDTO();
        dto.setCategoryId(2L);
        dto.setName("Cached Setmeal");
        dto.setPrice(new BigDecimal("28.00"));
        dto.setStatus(1);
        dto.setDishes(List.of(dishDTO));
        return setmealService.create(dto);
    }
}
