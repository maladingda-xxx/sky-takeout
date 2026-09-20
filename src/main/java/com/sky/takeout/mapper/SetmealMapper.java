package com.sky.takeout.mapper;

import com.sky.takeout.dto.SetmealPageQueryDTO;
import com.sky.takeout.entity.Setmeal;
import com.sky.takeout.entity.SetmealDish;
import com.sky.takeout.vo.SetmealDishVO;
import com.sky.takeout.vo.SetmealPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetmealMapper {

    long countByQuery(SetmealPageQueryDTO query);

    List<SetmealPageVO> selectPage(SetmealPageQueryDTO query);

    long countByCategoryAndName(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("excludeId") Long excludeId
    );

    int insert(Setmeal setmeal);

    Setmeal selectById(Long id);

    List<SetmealPageVO> selectEnabledByCategoryId(Long categoryId);

    int update(Setmeal setmeal);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("updateUser") Long updateUser
    );

    int deleteById(Long id);

    int insertDishes(@Param("dishes") List<SetmealDish> dishes);

    int deleteDishesBySetmealId(Long setmealId);

    List<SetmealDishVO> selectDishesBySetmealId(Long setmealId);
}
