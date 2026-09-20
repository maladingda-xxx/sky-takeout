package com.sky.takeout.service.impl;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.ShoppingCartItemDTO;
import com.sky.takeout.dto.ShoppingCartUpdateDTO;
import com.sky.takeout.entity.Dish;
import com.sky.takeout.entity.Setmeal;
import com.sky.takeout.entity.ShoppingCart;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.DishMapper;
import com.sky.takeout.mapper.SetmealMapper;
import com.sky.takeout.mapper.ShoppingCartMapper;
import com.sky.takeout.service.ShoppingCartService;
import com.sky.takeout.vo.ShoppingCartVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final int MAX_QUANTITY = 99;
    private static final int ENABLED = 1;

    private final ShoppingCartMapper shoppingCartMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    public ShoppingCartServiceImpl(
            ShoppingCartMapper shoppingCartMapper,
            DishMapper dishMapper,
            SetmealMapper setmealMapper
    ) {
        this.shoppingCartMapper = shoppingCartMapper;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
    }

    @Override
    @Transactional
    public void add(ShoppingCartItemDTO itemDTO) {
        Long userId = UserContext.getRequiredUserId();
        Product product = validateProduct(itemDTO);
        String flavor = normalizeFlavor(itemDTO.getFlavor());

        ShoppingCart existing = product.dish()
                ? shoppingCartMapper.selectByUserAndDish(userId, product.id())
                : shoppingCartMapper.selectByUserAndSetmeal(userId, product.id());

        if (existing != null) {
            int newQuantity = existing.getQuantity() + itemDTO.getQuantity();
            if (newQuantity > MAX_QUANTITY) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST.value(),
                        "Quantity must not exceed 99"
                );
            }

            shoppingCartMapper.updateQuantityAndFlavor(
                    existing.getId(),
                    userId,
                    newQuantity,
                    flavor == null ? existing.getFlavor() : flavor
            );
            return;
        }

        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(userId);
        cart.setDishId(product.dish() ? product.id() : null);
        cart.setSetmealId(product.dish() ? null : product.id());
        cart.setQuantity(itemDTO.getQuantity());
        cart.setFlavor(flavor);
        shoppingCartMapper.insert(cart);
    }

    @Override
    public List<ShoppingCartVO> list() {
        List<ShoppingCartVO> items = shoppingCartMapper.selectByUserId(
                UserContext.getRequiredUserId()
        );
        items.forEach(item -> item.setAmount(
                item.getUnitPrice().multiply(
                        BigDecimal.valueOf(item.getQuantity())
                )
        ));
        return items;
    }

    @Override
    @Transactional
    public void sub(ShoppingCartItemDTO itemDTO) {
        Long userId = UserContext.getRequiredUserId();
        Product product = validateProduct(itemDTO);
        ShoppingCart existing = product.dish()
                ? shoppingCartMapper.selectByUserAndDish(userId, product.id())
                : shoppingCartMapper.selectByUserAndSetmeal(userId, product.id());

        if (existing == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND.value(),
                    "Shopping cart item not found"
            );
        }

        if (existing.getQuantity() <= 1) {
            shoppingCartMapper.deleteByIdAndUserId(existing.getId(), userId);
            return;
        }

        shoppingCartMapper.updateQuantityAndFlavor(
                existing.getId(),
                userId,
                existing.getQuantity() - 1,
                existing.getFlavor()
        );
    }

    @Override
    @Transactional
    public void updateQuantity(ShoppingCartUpdateDTO updateDTO) {
        Long userId = UserContext.getRequiredUserId();
        ShoppingCart existing = shoppingCartMapper.selectByIdAndUserId(
                updateDTO.getId(),
                userId
        );
        if (existing == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND.value(),
                    "Shopping cart item not found"
            );
        }

        shoppingCartMapper.updateQuantityAndFlavor(
                existing.getId(),
                userId,
                updateDTO.getQuantity(),
                existing.getFlavor()
        );
    }

    @Override
    @Transactional
    public void clean() {
        shoppingCartMapper.deleteByUserId(UserContext.getRequiredUserId());
    }

    private Product validateProduct(ShoppingCartItemDTO itemDTO) {
        boolean hasDish = itemDTO.getDishId() != null;
        boolean hasSetmeal = itemDTO.getSetmealId() != null;
        if (hasDish == hasSetmeal) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Exactly one of dishId or setmealId is required"
            );
        }

        if (hasDish) {
            Dish dish = dishMapper.selectById(itemDTO.getDishId());
            if (dish == null || dish.getStatus() != ENABLED) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST.value(),
                        "Dish is not available"
                );
            }
            return new Product(itemDTO.getDishId(), true);
        }

        Setmeal setmeal = setmealMapper.selectById(itemDTO.getSetmealId());
        if (setmeal == null || setmeal.getStatus() != ENABLED) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Setmeal is not available"
            );
        }
        return new Product(itemDTO.getSetmealId(), false);
    }

    private String normalizeFlavor(String flavor) {
        if (flavor == null) {
            return null;
        }
        String normalized = flavor.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private record Product(Long id, boolean dish) {
    }
}
