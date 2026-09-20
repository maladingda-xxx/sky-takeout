package com.sky.takeout.mapper;

import com.sky.takeout.dto.DishPageQueryDTO;
import com.sky.takeout.entity.Dish;
import com.sky.takeout.entity.DishFlavor;
import com.sky.takeout.vo.DishPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishMapper {

    long countByQuery(DishPageQueryDTO query);

    List<DishPageVO> selectPage(DishPageQueryDTO query);

    int insert(Dish dish);

    Dish selectById(Long id);

    int update(Dish dish);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("updateUser") Long updateUser
    );

    int deleteById(Long id);

    int insertFlavors(@Param("flavors") List<DishFlavor> flavors);

    int deleteFlavorsByDishId(Long dishId);

    List<DishFlavor> selectFlavorsByDishId(Long dishId);

    List<DishFlavor> selectFlavorsByDishIds(@Param("dishIds") List<Long> dishIds);

    List<Dish> selectEnabledByCategoryId(Long categoryId);

    long countSetmealsByDishId(Long dishId);
}
