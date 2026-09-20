package com.sky.takeout.mapper;

import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.SetmealCreateDTO;
import com.sky.takeout.dto.SetmealDishDTO;
import com.sky.takeout.dto.SetmealPageQueryDTO;
import com.sky.takeout.dto.SetmealUpdateDTO;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.service.CategoryService;
import com.sky.takeout.service.DishService;
import com.sky.takeout.service.SetmealService;
import com.sky.takeout.vo.PageResult;
import com.sky.takeout.vo.SetmealDetailVO;
import com.sky.takeout.vo.SetmealPageVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SetmealMapperIntegrationTest {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Test
    void shouldCompleteSetmealCrudLifecycle() {
        Long dishId = createDish("Setmeal Dish");
        Long setmealId = setmealService.create(createDTO(dishId));

        SetmealDetailVO detail = setmealService.getById(setmealId);
        assertEquals("Set Meals", detail.getCategoryName());
        assertEquals("Family Meal", detail.getName());
        assertEquals(1, detail.getDishes().size());
        assertEquals(dishId, detail.getDishes().get(0).getDishId());

        SetmealPageQueryDTO query = new SetmealPageQueryDTO();
        query.setCategoryId(2L);
        query.setName("Family");
        PageResult<SetmealPageVO> page = setmealService.pageQuery(query);
        assertEquals(1, page.getTotal());
        assertEquals("Set Meals", page.getRecords().get(0).getCategoryName());

        SetmealUpdateDTO updateDTO = updateDTO(setmealId, dishId);
        setmealService.update(updateDTO);
        setmealService.updateStatus(setmealId, 1);

        SetmealDetailVO updated = setmealService.getById(setmealId);
        assertEquals("Updated Meal", updated.getName());
        assertEquals(1, updated.getStatus());
        assertEquals(2, updated.getDishes().get(0).getCopies());

        setmealService.delete(setmealId);
    }

    @Test
    void shouldRejectDishCategoryForSetmeal() {
        Long dishId = createDish("Category Guard Dish");
        SetmealCreateDTO createDTO = createDTO(dishId);
        createDTO.setCategoryId(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> setmealService.create(createDTO)
        );

        assertEquals(400, exception.getCode());
        assertEquals("Category type must be 2", exception.getMessage());
    }

    @Test
    void shouldPreventDeletingCategoryReferencedBySetmeal() {
        Long dishId = createDish("Setmeal Category Guard");
        setmealService.create(createDTO(dishId));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.delete(2L)
        );

        assertEquals(409, exception.getCode());
        assertEquals("Category is referenced by setmeals", exception.getMessage());
    }

    @Test
    void shouldPreventDeletingDishReferencedBySetmeal() {
        Long dishId = createDish("Referenced Dish");
        setmealService.create(createDTO(dishId));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> dishService.delete(dishId)
        );

        assertEquals(409, exception.getCode());
        assertEquals("Dish is referenced by setmeals", exception.getMessage());
    }

    private Long createDish(String name) {
        DishCreateDTO dishCreateDTO = new DishCreateDTO();
        dishCreateDTO.setName(name);
        dishCreateDTO.setCategoryId(1L);
        dishCreateDTO.setPrice(new BigDecimal("20.00"));
        dishCreateDTO.setStatus(0);
        return dishService.create(dishCreateDTO);
    }

    private SetmealCreateDTO createDTO(Long dishId) {
        SetmealDishDTO setmealDishDTO = new SetmealDishDTO();
        setmealDishDTO.setDishId(dishId);
        setmealDishDTO.setCopies(1);

        SetmealCreateDTO createDTO = new SetmealCreateDTO();
        createDTO.setCategoryId(2L);
        createDTO.setName("Family Meal");
        createDTO.setPrice(new BigDecimal("68.00"));
        createDTO.setStatus(0);
        createDTO.setDishes(List.of(setmealDishDTO));
        return createDTO;
    }

    private SetmealUpdateDTO updateDTO(Long setmealId, Long dishId) {
        SetmealDishDTO setmealDishDTO = new SetmealDishDTO();
        setmealDishDTO.setDishId(dishId);
        setmealDishDTO.setCopies(2);

        SetmealUpdateDTO updateDTO = new SetmealUpdateDTO();
        updateDTO.setId(setmealId);
        updateDTO.setCategoryId(2L);
        updateDTO.setName("Updated Meal");
        updateDTO.setPrice(new BigDecimal("72.00"));
        updateDTO.setDishes(List.of(setmealDishDTO));
        return updateDTO;
    }
}
