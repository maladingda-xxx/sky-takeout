package com.sky.takeout.service;

import com.sky.takeout.dto.CategoryCreateDTO;
import com.sky.takeout.dto.CategoryPageQueryDTO;
import com.sky.takeout.dto.CategoryUpdateDTO;
import com.sky.takeout.vo.CategoryVO;
import com.sky.takeout.vo.PageResult;

import java.util.List;

public interface CategoryService {

    PageResult<CategoryVO> pageQuery(CategoryPageQueryDTO query);

    Long create(CategoryCreateDTO categoryCreateDTO);

    CategoryVO getById(Long id);

    List<CategoryVO> listByType(Integer type);

    void update(CategoryUpdateDTO categoryUpdateDTO);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
