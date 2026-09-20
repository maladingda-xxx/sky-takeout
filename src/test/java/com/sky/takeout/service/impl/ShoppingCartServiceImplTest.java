package com.sky.takeout.service.impl;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.ShoppingCartItemDTO;
import com.sky.takeout.entity.Dish;
import com.sky.takeout.entity.ShoppingCart;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.DishMapper;
import com.sky.takeout.mapper.SetmealMapper;
import com.sky.takeout.mapper.ShoppingCartMapper;
import com.sky.takeout.vo.ShoppingCartVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    private ShoppingCartServiceImpl shoppingCartService;

    @BeforeEach
    void setUp() {
        shoppingCartService = new ShoppingCartServiceImpl(
                shoppingCartMapper,
                dishMapper,
                setmealMapper
        );
        UserContext.setUserId(7L);
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void shouldAddNewDishToCart() {
        ShoppingCartItemDTO itemDTO = dishItem(10L, 2);
        when(dishMapper.selectById(10L)).thenReturn(dish(10L));
        when(shoppingCartMapper.selectByUserAndDish(7L, 10L)).thenReturn(null);

        shoppingCartService.add(itemDTO);

        verify(shoppingCartMapper).insert(any(ShoppingCart.class));
    }

    @Test
    void shouldIncrementExistingCartItem() {
        ShoppingCartItemDTO itemDTO = dishItem(10L, 2);
        ShoppingCart existing = cart(1L, 10L, null, 3, null);
        when(dishMapper.selectById(10L)).thenReturn(dish(10L));
        when(shoppingCartMapper.selectByUserAndDish(7L, 10L)).thenReturn(existing);

        shoppingCartService.add(itemDTO);

        verify(shoppingCartMapper).updateQuantityAndFlavor(1L, 7L, 5, null);
    }

    @Test
    void shouldRejectCartItemWithoutExactlyOneProduct() {
        ShoppingCartItemDTO itemDTO = new ShoppingCartItemDTO();
        itemDTO.setDishId(10L);
        itemDTO.setSetmealId(20L);
        itemDTO.setQuantity(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> shoppingCartService.add(itemDTO)
        );

        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldDeleteItemWhenSubtractingLastCopy() {
        ShoppingCartItemDTO itemDTO = dishItem(10L, 1);
        ShoppingCart existing = cart(1L, 10L, null, 1, null);
        when(dishMapper.selectById(10L)).thenReturn(dish(10L));
        when(shoppingCartMapper.selectByUserAndDish(7L, 10L)).thenReturn(existing);

        shoppingCartService.sub(itemDTO);

        verify(shoppingCartMapper).deleteByIdAndUserId(1L, 7L);
    }

    @Test
    void shouldCalculateCartAmount() {
        ShoppingCartVO item = new ShoppingCartVO();
        item.setId(1L);
        item.setUnitPrice(new BigDecimal("18.50"));
        item.setQuantity(3);
        when(shoppingCartMapper.selectByUserId(7L)).thenReturn(List.of(item));

        List<ShoppingCartVO> result = shoppingCartService.list();

        assertEquals(new BigDecimal("55.50"), result.get(0).getAmount());
    }

    @Test
    void shouldCleanCurrentUserCart() {
        shoppingCartService.clean();

        verify(shoppingCartMapper).deleteByUserId(7L);
    }

    private ShoppingCartItemDTO dishItem(Long dishId, int quantity) {
        ShoppingCartItemDTO itemDTO = new ShoppingCartItemDTO();
        itemDTO.setDishId(dishId);
        itemDTO.setQuantity(quantity);
        return itemDTO;
    }

    private Dish dish(Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(1);
        return dish;
    }

    private ShoppingCart cart(
            Long id,
            Long dishId,
            Long setmealId,
            int quantity,
            String flavor
    ) {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(id);
        cart.setDishId(dishId);
        cart.setSetmealId(setmealId);
        cart.setQuantity(quantity);
        cart.setFlavor(flavor);
        return cart;
    }
}
