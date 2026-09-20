package com.sky.takeout.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.takeout.entity.Category;
import com.sky.takeout.entity.Dish;
import com.sky.takeout.entity.DishFlavor;
import com.sky.takeout.entity.Setmeal;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.CategoryMapper;
import com.sky.takeout.mapper.DishMapper;
import com.sky.takeout.mapper.SetmealMapper;
import com.sky.takeout.vo.DishUserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCatalogServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    private UserCatalogServiceImpl userCatalogService;

    @BeforeEach
    void setUp() {
        userCatalogService = new UserCatalogServiceImpl(
                categoryMapper,
                dishMapper,
                setmealMapper,
                new ObjectMapper()
        );
    }

    @Test
    void shouldMapEnabledDishAndFlavors() {
        Category category = category(1L, 1, 1);
        Dish dish = dish(10L, 1L);
        DishFlavor flavor = new DishFlavor();
        flavor.setDishId(10L);
        flavor.setName("Spiciness");
        flavor.setValue("[\"Mild\",\"Spicy\"]");

        when(categoryMapper.selectById(1L)).thenReturn(category);
        when(dishMapper.selectEnabledByCategoryId(1L)).thenReturn(List.of(dish));
        when(dishMapper.selectFlavorsByDishIds(List.of(10L)))
                .thenReturn(List.of(flavor));

        List<DishUserVO> dishes = userCatalogService.listDishes(1L);

        assertEquals(1, dishes.size());
        assertEquals("Dish", dishes.get(0).getName());
        assertEquals(List.of("Mild", "Spicy"), dishes.get(0).getFlavors().get(0).getValue());
    }

    @Test
    void shouldRejectDisabledCategory() {
        when(categoryMapper.selectById(1L)).thenReturn(category(1L, 1, 0));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userCatalogService.listDishes(1L)
        );

        assertEquals(404, exception.getCode());
    }

    @Test
    void shouldRejectDisabledSetmealDetail() {
        Setmeal setmeal = new Setmeal();
        setmeal.setId(5L);
        setmeal.setStatus(0);
        when(setmealMapper.selectById(5L)).thenReturn(setmeal);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userCatalogService.getSetmealDetail(5L)
        );

        assertEquals(404, exception.getCode());
        assertEquals("Setmeal not found", exception.getMessage());
    }

    private Category category(Long id, Integer type, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setType(type);
        category.setStatus(status);
        category.setName("Category");
        return category;
    }

    private Dish dish(Long id, Long categoryId) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setCategoryId(categoryId);
        dish.setName("Dish");
        dish.setPrice(new BigDecimal("18.00"));
        dish.setStatus(1);
        return dish;
    }
}
