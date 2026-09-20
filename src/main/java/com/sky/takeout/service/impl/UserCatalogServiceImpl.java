package com.sky.takeout.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.takeout.entity.Category;
import com.sky.takeout.entity.Dish;
import com.sky.takeout.entity.DishFlavor;
import com.sky.takeout.entity.Setmeal;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.CategoryMapper;
import com.sky.takeout.mapper.DishMapper;
import com.sky.takeout.mapper.SetmealMapper;
import com.sky.takeout.service.UserCatalogService;
import com.sky.takeout.vo.CategoryVO;
import com.sky.takeout.vo.DishFlavorVO;
import com.sky.takeout.vo.DishUserVO;
import com.sky.takeout.vo.SetmealDetailVO;
import com.sky.takeout.vo.SetmealPageVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserCatalogServiceImpl implements UserCatalogService {

    private static final int ENABLED = 1;

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final ObjectMapper objectMapper;

    public UserCatalogServiceImpl(
            CategoryMapper categoryMapper,
            DishMapper dishMapper,
            SetmealMapper setmealMapper,
            ObjectMapper objectMapper
    ) {
        this.categoryMapper = categoryMapper;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CategoryVO> listCategories(Integer type) {
        validateCategoryType(type);
        return categoryMapper.selectEnabledByType(type)
                .stream()
                .map(this::toCategoryVO)
                .toList();
    }

    @Override
    public List<DishUserVO> listDishes(Long categoryId) {
        requireEnabledCategory(categoryId, 1);
        List<Dish> dishes = dishMapper.selectEnabledByCategoryId(categoryId);
        if (dishes.isEmpty()) {
            return List.of();
        }

        List<Long> dishIds = dishes.stream().map(Dish::getId).toList();
        Map<Long, List<DishFlavorVO>> flavorsByDishId = dishMapper
                .selectFlavorsByDishIds(dishIds)
                .stream()
                .collect(Collectors.groupingBy(
                        DishFlavor::getDishId,
                        Collectors.mapping(this::toFlavorVO, Collectors.toList())
                ));

        return dishes.stream()
                .map(dish -> new DishUserVO(
                        dish.getId(),
                        dish.getName(),
                        dish.getPrice(),
                        dish.getImage(),
                        dish.getDescription(),
                        flavorsByDishId.getOrDefault(dish.getId(), List.of())
                ))
                .toList();
    }

    @Override
    public List<SetmealPageVO> listSetmeals(Long categoryId) {
        requireEnabledCategory(categoryId, 2);
        return setmealMapper.selectEnabledByCategoryId(categoryId);
    }

    @Override
    public SetmealDetailVO getSetmealDetail(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        if (setmeal == null || setmeal.getStatus() != ENABLED) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND.value(),
                    "Setmeal not found"
            );
        }

        Category category = categoryMapper.selectById(setmeal.getCategoryId());
        return new SetmealDetailVO(
                setmeal.getId(),
                setmeal.getCategoryId(),
                category == null ? null : category.getName(),
                setmeal.getName(),
                setmeal.getPrice(),
                setmeal.getStatus(),
                setmeal.getDescription(),
                setmeal.getImage(),
                setmeal.getUpdateTime(),
                setmealMapper.selectDishesBySetmealId(id)
        );
    }

    private Category requireEnabledCategory(Long categoryId, int expectedType) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getStatus() != ENABLED) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND.value(),
                    "Category not found"
            );
        }

        if (category.getType() != expectedType) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Category type mismatch"
            );
        }

        return category;
    }

    private void validateCategoryType(Integer type) {
        if (type == null || (type != 1 && type != 2)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Type must be 1 or 2"
            );
        }
    }

    private CategoryVO toCategoryVO(Category category) {
        return new CategoryVO(
                category.getId(),
                category.getType(),
                category.getName(),
                category.getSort(),
                category.getStatus(),
                category.getUpdateTime()
        );
    }

    private DishFlavorVO toFlavorVO(DishFlavor flavor) {
        return new DishFlavorVO(
                flavor.getName(),
                readFlavorValue(flavor.getValue())
        );
    }

    private List<String> readFlavorValue(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to deserialize flavor values"
            );
        }
    }
}
