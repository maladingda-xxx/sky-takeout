package com.sky.takeout.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.DishFlavorDTO;
import com.sky.takeout.dto.DishPageQueryDTO;
import com.sky.takeout.dto.DishUpdateDTO;
import com.sky.takeout.entity.Category;
import com.sky.takeout.entity.Dish;
import com.sky.takeout.entity.DishFlavor;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.CategoryMapper;
import com.sky.takeout.mapper.DishMapper;
import com.sky.takeout.vo.DishPageVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishServiceImplTest {

    @Mock
    private DishMapper dishMapper;

    @Mock
    private CategoryMapper categoryMapper;

    private DishServiceImpl dishService;

    @BeforeEach
    void setUp() {
        dishService = new DishServiceImpl(
                dishMapper,
                categoryMapper,
                new ObjectMapper()
        );
    }

    @Test
    void shouldReturnEmptyPageWithoutSelectingRows() {
        DishPageQueryDTO query = new DishPageQueryDTO();
        when(dishMapper.countByQuery(query)).thenReturn(0L);

        PageResult<DishPageVO> result = dishService.pageQuery(query);

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(dishMapper, never()).selectPage(query);
    }

    @Test
    void shouldRejectNonDishCategory() {
        DishCreateDTO createDTO = createDTO(2L);
        when(categoryMapper.selectById(2L)).thenReturn(category(2L, 2));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> dishService.create(createDTO)
        );

        assertEquals(400, exception.getCode());
        assertEquals("Category type must be 1", exception.getMessage());
        verify(dishMapper, never()).insert(any(Dish.class));
    }

    @Test
    void shouldCreateDishAndSerializeFlavors() {
        DishCreateDTO createDTO = createDTO(1L);
        when(categoryMapper.selectById(1L)).thenReturn(category(1L, 1));
        when(dishMapper.insert(any(Dish.class))).thenAnswer(invocation -> {
            Dish dish = invocation.getArgument(0);
            dish.setId(10L);
            return 1;
        });
        when(dishMapper.insertFlavors(any())).thenReturn(1);

        Long id = dishService.create(createDTO);

        assertEquals(10L, id);
        ArgumentCaptor<Dish> dishCaptor = ArgumentCaptor.forClass(Dish.class);
        verify(dishMapper).insert(dishCaptor.capture());
        assertEquals("Spicy Chicken", dishCaptor.getValue().getName());
        assertEquals(0, dishCaptor.getValue().getStatus());

        ArgumentCaptor<List<DishFlavor>> flavorCaptor = ArgumentCaptor.forClass(List.class);
        verify(dishMapper).insertFlavors(flavorCaptor.capture());
        assertEquals("[\"Spicy\",\"Mild\"]", flavorCaptor.getValue().get(0).getValue());
    }

    @Test
    void shouldRejectUpdateWhenDishDoesNotExist() {
        DishUpdateDTO updateDTO = new DishUpdateDTO();
        updateDTO.setId(99L);
        when(dishMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> dishService.update(updateDTO)
        );

        assertEquals(404, exception.getCode());
        assertEquals("Dish not found", exception.getMessage());
    }

    @Test
    void shouldReplaceFlavorsWhenUpdatingDish() {
        DishUpdateDTO updateDTO = updateDTO(1L);
        when(dishMapper.selectById(1L)).thenReturn(dish(1L, 1L));
        when(categoryMapper.selectById(1L)).thenReturn(category(1L, 1));
        when(dishMapper.update(any(Dish.class))).thenReturn(1);
        when(dishMapper.insertFlavors(any())).thenReturn(1);

        dishService.update(updateDTO);

        verify(dishMapper).deleteFlavorsByDishId(1L);
        verify(dishMapper).insertFlavors(any());
    }

    @Test
    void shouldDeleteDishAndItsFlavors() {
        when(dishMapper.selectById(1L)).thenReturn(dish(1L, 1L));
        when(dishMapper.deleteById(1L)).thenReturn(1);

        dishService.delete(1L);

        verify(dishMapper).deleteFlavorsByDishId(1L);
        verify(dishMapper).deleteById(1L);
    }

    private DishCreateDTO createDTO(Long categoryId) {
        DishCreateDTO createDTO = new DishCreateDTO();
        createDTO.setName("Spicy Chicken");
        createDTO.setCategoryId(categoryId);
        createDTO.setPrice(new BigDecimal("28.50"));
        createDTO.setStatus(0);

        DishFlavorDTO flavorDTO = new DishFlavorDTO();
        flavorDTO.setName("Spiciness");
        flavorDTO.setValue(List.of("Spicy", "Mild"));
        createDTO.setFlavors(List.of(flavorDTO));
        return createDTO;
    }

    private DishUpdateDTO updateDTO(Long categoryId) {
        DishUpdateDTO updateDTO = new DishUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setName("Updated Chicken");
        updateDTO.setCategoryId(categoryId);
        updateDTO.setPrice(new BigDecimal("30.00"));

        DishFlavorDTO flavorDTO = new DishFlavorDTO();
        flavorDTO.setName("Spiciness");
        flavorDTO.setValue(List.of("Mild"));
        updateDTO.setFlavors(List.of(flavorDTO));
        return updateDTO;
    }

    private Category category(Long id, Integer type) {
        Category category = new Category();
        category.setId(id);
        category.setType(type);
        category.setName("Category");
        return category;
    }

    private Dish dish(Long id, Long categoryId) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setCategoryId(categoryId);
        return dish;
    }
}
