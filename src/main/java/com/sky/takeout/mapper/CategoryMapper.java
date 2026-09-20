package com.sky.takeout.mapper;

import com.sky.takeout.dto.CategoryPageQueryDTO;
import com.sky.takeout.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    long countByQuery(CategoryPageQueryDTO query);

    List<Category> selectPage(CategoryPageQueryDTO query);

    long countByTypeAndName(
            @Param("type") Integer type,
            @Param("name") String name,
            @Param("excludeId") Long excludeId
    );

    int insert(Category category);

    Category selectById(Long id);

    List<Category> selectByType(Integer type);

    List<Category> selectEnabledByType(Integer type);

    int update(Category category);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("updateUser") Long updateUser
    );

    long countDishesByCategoryId(Long categoryId);

    long countSetmealsByCategoryId(Long categoryId);

    int deleteById(Long id);
}
