package com.sky.takeout.mapper;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.SetmealCreateDTO;
import com.sky.takeout.dto.SetmealDishDTO;
import com.sky.takeout.dto.ShoppingCartItemDTO;
import com.sky.takeout.dto.ShoppingCartUpdateDTO;
import com.sky.takeout.service.DishService;
import com.sky.takeout.service.SetmealService;
import com.sky.takeout.service.ShoppingCartService;
import com.sky.takeout.vo.ShoppingCartVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShoppingCartIntegrationTest {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @BeforeEach
    void setUpContext() {
        UserContext.setUserId(99L);
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void shouldCompleteShoppingCartLifecycle() {
        Long dishId = createDish();
        Long setmealId = createSetmeal(dishId);

        shoppingCartService.add(item(dishId, null, 2));
        shoppingCartService.add(item(dishId, null, 1));
        shoppingCartService.add(item(null, setmealId, 1));

        List<ShoppingCartVO> items = shoppingCartService.list();
        assertEquals(2, items.size());
        assertEquals(3, items.get(0).getQuantity());
        assertEquals(new BigDecimal("54.00"), items.get(0).getAmount());

        ShoppingCartUpdateDTO updateDTO = new ShoppingCartUpdateDTO();
        updateDTO.setId(items.get(0).getId());
        updateDTO.setQuantity(4);
        shoppingCartService.updateQuantity(updateDTO);
        assertEquals(4, shoppingCartService.list().get(0).getQuantity());

        shoppingCartService.sub(item(dishId, null, 1));
        assertEquals(3, shoppingCartService.list().get(0).getQuantity());

        shoppingCartService.clean();
        assertTrue(shoppingCartService.list().isEmpty());
    }

    private Long createDish() {
        DishCreateDTO dto = new DishCreateDTO();
        dto.setName("Cart Dish");
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
        dto.setName("Cart Setmeal");
        dto.setPrice(new BigDecimal("50.00"));
        dto.setStatus(1);
        dto.setDishes(List.of(dishDTO));
        return setmealService.create(dto);
    }

    private ShoppingCartItemDTO item(Long dishId, Long setmealId, int quantity) {
        ShoppingCartItemDTO dto = new ShoppingCartItemDTO();
        dto.setDishId(dishId);
        dto.setSetmealId(setmealId);
        dto.setQuantity(quantity);
        return dto;
    }
}
