package com.sky.takeout.service;

import com.sky.takeout.dto.SetmealCreateDTO;
import com.sky.takeout.dto.SetmealPageQueryDTO;
import com.sky.takeout.dto.SetmealUpdateDTO;
import com.sky.takeout.vo.PageResult;
import com.sky.takeout.vo.SetmealDetailVO;
import com.sky.takeout.vo.SetmealPageVO;

public interface SetmealService {

    PageResult<SetmealPageVO> pageQuery(SetmealPageQueryDTO query);

    Long create(SetmealCreateDTO setmealCreateDTO);

    SetmealDetailVO getById(Long id);

    void update(SetmealUpdateDTO setmealUpdateDTO);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
