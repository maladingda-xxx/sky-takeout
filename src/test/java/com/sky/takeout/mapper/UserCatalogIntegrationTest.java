package com.sky.takeout.mapper;

import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.SetmealCreateDTO;
import com.sky.takeout.dto.SetmealDishDTO;
import com.sky.takeout.service.DishService;
import com.sky.takeout.service.SetmealService;
import com.sky.takeout.service.UserCatalogService;
import com.sky.takeout.vo.DishUserVO;
import com.sky.takeout.vo.SetmealPageVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserCatalogIntegrationTest {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private UserCatalogService userCatalogService;

    @Test
    void shouldReturnOnlyEnabledDishesAndSetmeals() {
        Long enabledDishId = dishService.create(dishDTO("Enabled Dish", 1));
        dishService.create(dishDTO("Disabled Dish", 0));

        List<DishUserVO> dishes = userCatalogService.listDishes(1L);

        assertEquals(1, dishes.size());
        assertEquals(enabledDishId, dishes.get(0).getId());

        Long enabledSetmealId = setmealService.create(setmealDTO(
                enabledDishId,
                "Enabled Setmeal",
                1
        ));
        setmealService.create(setmealDTO(
                enabledDishId,
                "Disabled Setmeal",
                0
        ));

        List<SetmealPageVO> setmeals = userCatalogService.listSetmeals(2L);

        assertEquals(1, setmeals.size());
        assertEquals(enabledSetmealId, setmeals.get(0).getId());
    }

    private DishCreateDTO dishDTO(String name, int status) {
        DishCreateDTO createDTO = new DishCreateDTO();
        createDTO.setName(name);
        createDTO.setCategoryId(1L);
        createDTO.setPrice(new BigDecimal("18.00"));
        createDTO.setStatus(status);
        return createDTO;
    }

    private SetmealCreateDTO setmealDTO(Long dishId, String name, int status) {
        SetmealDishDTO dishDTO = new SetmealDishDTO();
        dishDTO.setDishId(dishId);
        dishDTO.setCopies(1);

        SetmealCreateDTO createDTO = new SetmealCreateDTO();
        createDTO.setCategoryId(2L);
        createDTO.setName(name);
        createDTO.setPrice(new BigDecimal("58.00"));
        createDTO.setStatus(status);
        createDTO.setDishes(List.of(dishDTO));
        return createDTO;
    }
}
