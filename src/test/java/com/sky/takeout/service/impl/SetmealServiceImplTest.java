package com.sky.takeout.service.impl;

import com.sky.takeout.dto.SetmealCreateDTO;
import com.sky.takeout.dto.SetmealDishDTO;
import com.sky.takeout.dto.SetmealPageQueryDTO;
import com.sky.takeout.dto.SetmealUpdateDTO;
import com.sky.takeout.entity.Category;
import com.sky.takeout.entity.Dish;
import com.sky.takeout.entity.Setmeal;
import com.sky.takeout.entity.SetmealDish;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.CategoryMapper;
import com.sky.takeout.mapper.DishMapper;
import com.sky.takeout.mapper.SetmealMapper;
import com.sky.takeout.vo.PageResult;
import com.sky.takeout.vo.SetmealPageVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
class SetmealServiceImplTest {

    @Mock
    private SetmealMapper setmealMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private DishMapper dishMapper;

    @InjectMocks
    private SetmealServiceImpl setmealService;

    @Test
    void shouldReturnEmptyPageWithoutSelectingRows() {
        SetmealPageQueryDTO query = new SetmealPageQueryDTO();
        when(setmealMapper.countByQuery(query)).thenReturn(0L);

        PageResult<SetmealPageVO> result = setmealService.pageQuery(query);

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(setmealMapper, never()).selectPage(query);
    }

    @Test
    void shouldRejectDishCategoryForSetmeal() {
        SetmealCreateDTO createDTO = createDTO(1L);
        when(categoryMapper.selectById(1L)).thenReturn(category(1L, 1));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> setmealService.create(createDTO)
        );

        assertEquals(400, exception.getCode());
        assertEquals("Category type must be 2", exception.getMessage());
    }

    @Test
    void shouldCreateSetmealWithDishes() {
        SetmealCreateDTO createDTO = createDTO(2L);
        when(categoryMapper.selectById(2L)).thenReturn(category(2L, 2));
        when(setmealMapper.countByCategoryAndName(2L, "Family Meal", null))
                .thenReturn(0L);
        when(dishMapper.selectById(10L)).thenReturn(dish(10L));
        when(dishMapper.selectById(11L)).thenReturn(dish(11L));
        when(setmealMapper.insert(any(Setmeal.class))).thenAnswer(invocation -> {
            Setmeal setmeal = invocation.getArgument(0);
            setmeal.setId(5L);
            return 1;
        });
        when(setmealMapper.insertDishes(any())).thenReturn(2);

        Long id = setmealService.create(createDTO);

        assertEquals(5L, id);
        ArgumentCaptor<Setmeal> setmealCaptor = ArgumentCaptor.forClass(Setmeal.class);
        verify(setmealMapper).insert(setmealCaptor.capture());
        assertEquals("Family Meal", setmealCaptor.getValue().getName());
        assertEquals(0, setmealCaptor.getValue().getStatus());

        ArgumentCaptor<List<SetmealDish>> dishCaptor = ArgumentCaptor.forClass(List.class);
        verify(setmealMapper).insertDishes(dishCaptor.capture());
        assertEquals(5L, dishCaptor.getValue().get(0).getSetmealId());
        assertEquals(2, dishCaptor.getValue().get(1).getCopies());
    }

    @Test
    void shouldRejectDuplicateSetmealNameInCategory() {
        SetmealCreateDTO createDTO = createDTO(2L);
        when(categoryMapper.selectById(2L)).thenReturn(category(2L, 2));
        when(setmealMapper.countByCategoryAndName(2L, "Family Meal", null))
                .thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> setmealService.create(createDTO)
        );

        assertEquals(409, exception.getCode());
        verify(setmealMapper, never()).insert(any(Setmeal.class));
    }

    @Test
    void shouldRejectUpdateWhenSetmealDoesNotExist() {
        SetmealUpdateDTO updateDTO = new SetmealUpdateDTO();
        updateDTO.setId(99L);
        when(setmealMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> setmealService.update(updateDTO)
        );

        assertEquals(404, exception.getCode());
        assertEquals("Setmeal not found", exception.getMessage());
    }

    @Test
    void shouldDeleteSetmealAndItsDishRelations() {
        when(setmealMapper.selectById(5L)).thenReturn(setmeal(5L));
        when(setmealMapper.deleteById(5L)).thenReturn(1);

        setmealService.delete(5L);

        verify(setmealMapper).deleteDishesBySetmealId(5L);
        verify(setmealMapper).deleteById(5L);
    }

    private SetmealCreateDTO createDTO(Long categoryId) {
        SetmealDishDTO dishOne = new SetmealDishDTO();
        dishOne.setDishId(10L);
        dishOne.setCopies(1);

        SetmealDishDTO dishTwo = new SetmealDishDTO();
        dishTwo.setDishId(11L);
        dishTwo.setCopies(2);

        SetmealCreateDTO createDTO = new SetmealCreateDTO();
        createDTO.setCategoryId(categoryId);
        createDTO.setName("Family Meal");
        createDTO.setPrice(new BigDecimal("68.00"));
        createDTO.setStatus(0);
        createDTO.setDishes(List.of(dishOne, dishTwo));
        return createDTO;
    }

    private Category category(Long id, Integer type) {
        Category category = new Category();
        category.setId(id);
        category.setType(type);
        category.setName("Category");
        return category;
    }

    private Dish dish(Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName("Dish " + id);
        return dish;
    }

    private Setmeal setmeal(Long id) {
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        return setmeal;
    }
}
