package com.sky.takeout.service;

import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.DishPageQueryDTO;
import com.sky.takeout.dto.DishUpdateDTO;
import com.sky.takeout.vo.DishDetailVO;
import com.sky.takeout.vo.DishPageVO;
import com.sky.takeout.vo.PageResult;

public interface DishService {

    PageResult<DishPageVO> pageQuery(DishPageQueryDTO query);

    Long create(DishCreateDTO dishCreateDTO);

    DishDetailVO getById(Long id);

    void update(DishUpdateDTO dishUpdateDTO);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
