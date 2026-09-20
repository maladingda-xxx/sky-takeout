package com.sky.takeout.mapper;

import com.sky.takeout.entity.ShoppingCart;
import com.sky.takeout.vo.ShoppingCartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    ShoppingCart selectByUserAndDish(
            @Param("userId") Long userId,
            @Param("dishId") Long dishId
    );

    ShoppingCart selectByUserAndSetmeal(
            @Param("userId") Long userId,
            @Param("setmealId") Long setmealId
    );

    ShoppingCart selectByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    int insert(ShoppingCart shoppingCart);

    int updateQuantityAndFlavor(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("quantity") Integer quantity,
            @Param("flavor") String flavor
    );

    int deleteByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    int deleteByUserId(Long userId);

    List<ShoppingCartVO> selectByUserId(Long userId);
}
