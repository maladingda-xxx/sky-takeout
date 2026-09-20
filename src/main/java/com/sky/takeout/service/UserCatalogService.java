package com.sky.takeout.service;

import com.sky.takeout.vo.CategoryVO;
import com.sky.takeout.vo.DishUserVO;
import com.sky.takeout.vo.SetmealDetailVO;
import com.sky.takeout.vo.SetmealPageVO;

import java.util.List;

public interface UserCatalogService {

    List<CategoryVO> listCategories(Integer type);

    List<DishUserVO> listDishes(Long categoryId);

    List<SetmealPageVO> listSetmeals(Long categoryId);

    SetmealDetailVO getSetmealDetail(Long id);
}
