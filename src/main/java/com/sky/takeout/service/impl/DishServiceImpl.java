package com.sky.takeout.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.takeout.common.CacheNames;
import com.sky.takeout.common.CurrentUserContext;
import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.DishFlavorDTO;
import com.sky.takeout.dto.DishPageQueryDTO;
import com.sky.takeout.dto.DishUpdateDTO;
import com.sky.takeout.entity.Category;
import com.sky.takeout.entity.Dish;
import com.sky.takeout.entity.DishFlavor;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.CategoryMapper;
import com.sky.takeout.mapper.DishMapper;
import com.sky.takeout.service.DishService;
import com.sky.takeout.vo.DishDetailVO;
import com.sky.takeout.vo.DishFlavorVO;
import com.sky.takeout.vo.DishPageVO;
import com.sky.takeout.vo.PageResult;
import org.springframework.http.HttpStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DISH_CATEGORY_TYPE = 1;

    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;

    public DishServiceImpl(
            DishMapper dishMapper,
            CategoryMapper categoryMapper,
            ObjectMapper objectMapper
    ) {
        this.dishMapper = dishMapper;
        this.categoryMapper = categoryMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResult<DishPageVO> pageQuery(DishPageQueryDTO query) {
        normalizeQuery(query);

        long total = dishMapper.countByQuery(query);
        if (total == 0) {
            return new PageResult<>(0, List.of());
        }

        return new PageResult<>(total, dishMapper.selectPage(query));
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    CacheNames.DISHES,
                    CacheNames.SETMEAL_DETAIL,
                    CacheNames.CATALOG_MISS
            },
            allEntries = true
    )
    public Long create(DishCreateDTO dishCreateDTO) {
        requireDishCategory(dishCreateDTO.getCategoryId());

        Dish dish = new Dish();
        dish.setName(dishCreateDTO.getName().trim());
        dish.setCategoryId(dishCreateDTO.getCategoryId());
        dish.setPrice(dishCreateDTO.getPrice());
        dish.setImage(normalizeOptionalText(dishCreateDTO.getImage()));
        dish.setDescription(normalizeOptionalText(dishCreateDTO.getDescription()));
        dish.setStatus(dishCreateDTO.getStatus());
        long currentUserId = CurrentUserContext.getUserIdOrDefault();
        dish.setCreateUser(currentUserId);
        dish.setUpdateUser(currentUserId);

        if (dishMapper.insert(dish) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create dish"
            );
        }

        saveFlavors(dish.getId(), dishCreateDTO.getFlavors());
        return dish.getId();
    }

    @Override
    public DishDetailVO getById(Long id) {
        Dish dish = requireDish(id);
        Category category = categoryMapper.selectById(dish.getCategoryId());

        return new DishDetailVO(
                dish.getId(),
                dish.getName(),
                dish.getCategoryId(),
                category == null ? null : category.getName(),
                dish.getPrice(),
                dish.getImage(),
                dish.getDescription(),
                dish.getStatus(),
                dish.getUpdateTime(),
                readFlavors(dish.getId())
        );
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    CacheNames.DISHES,
                    CacheNames.SETMEAL_DETAIL,
                    CacheNames.CATALOG_MISS
            },
            allEntries = true
    )
    public void update(DishUpdateDTO dishUpdateDTO) {
        requireDish(dishUpdateDTO.getId());
        requireDishCategory(dishUpdateDTO.getCategoryId());

        Dish dish = new Dish();
        dish.setId(dishUpdateDTO.getId());
        dish.setName(dishUpdateDTO.getName().trim());
        dish.setCategoryId(dishUpdateDTO.getCategoryId());
        dish.setPrice(dishUpdateDTO.getPrice());
        dish.setImage(normalizeOptionalText(dishUpdateDTO.getImage()));
        dish.setDescription(normalizeOptionalText(dishUpdateDTO.getDescription()));
        dish.setUpdateUser(CurrentUserContext.getUserIdOrDefault());

        if (dishMapper.update(dish) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to update dish"
            );
        }

        dishMapper.deleteFlavorsByDishId(dish.getId());
        saveFlavors(dish.getId(), dishUpdateDTO.getFlavors());
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    CacheNames.DISHES,
                    CacheNames.SETMEAL_DETAIL,
                    CacheNames.CATALOG_MISS
            },
            allEntries = true
    )
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Status must be 0 or 1"
            );
        }

        requireDish(id);

        if (dishMapper.updateStatus(
                id,
                status,
                CurrentUserContext.getUserIdOrDefault()
        ) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to update dish status"
            );
        }
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    CacheNames.DISHES,
                    CacheNames.SETMEAL_DETAIL,
                    CacheNames.CATALOG_MISS
            },
            allEntries = true
    )
    public void delete(Long id) {
        requireDish(id);

        if (dishMapper.countSetmealsByDishId(id) > 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Dish is referenced by setmeals"
            );
        }

        dishMapper.deleteFlavorsByDishId(id);
        if (dishMapper.deleteById(id) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to delete dish"
            );
        }
    }

    private void normalizeQuery(DishPageQueryDTO query) {
        query.setPage(Math.max(query.getPage(), 1));
        query.setPageSize(Math.min(Math.max(query.getPageSize(), 1), MAX_PAGE_SIZE));

        if (query.getName() != null) {
            String name = query.getName().trim();
            query.setName(name.isEmpty() ? null : name);
        }
    }

    private Dish requireDish(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND.value(),
                    "Dish not found"
            );
        }
        return dish;
    }

    private Category requireDishCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Dish category not found"
            );
        }

        if (category.getType() != DISH_CATEGORY_TYPE) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Category type must be 1"
            );
        }

        return category;
    }

    private void saveFlavors(Long dishId, List<DishFlavorDTO> flavorDTOs) {
        if (flavorDTOs == null || flavorDTOs.isEmpty()) {
            return;
        }

        List<DishFlavor> flavors = new ArrayList<>(flavorDTOs.size());
        for (DishFlavorDTO flavorDTO : flavorDTOs) {
            DishFlavor flavor = new DishFlavor();
            flavor.setDishId(dishId);
            flavor.setName(flavorDTO.getName().trim());
            flavor.setValue(writeFlavorValue(flavorDTO.getValue()));
            long currentUserId = CurrentUserContext.getUserIdOrDefault();
            flavor.setCreateUser(currentUserId);
            flavor.setUpdateUser(currentUserId);
            flavors.add(flavor);
        }

        if (dishMapper.insertFlavors(flavors) != flavors.size()) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create dish flavors"
            );
        }
    }

    private List<DishFlavorVO> readFlavors(Long dishId) {
        return dishMapper.selectFlavorsByDishId(dishId)
                .stream()
                .map(flavor -> new DishFlavorVO(
                        flavor.getName(),
                        readFlavorValue(flavor.getValue())
                ))
                .toList();
    }

    private String writeFlavorValue(List<String> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to serialize flavor values"
            );
        }
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

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
