package com.sky.takeout.service.impl;

import com.sky.takeout.common.CurrentUserContext;
import com.sky.takeout.common.CacheNames;
import com.sky.takeout.dto.SetmealCreateDTO;
import com.sky.takeout.dto.SetmealDishDTO;
import com.sky.takeout.dto.SetmealPageQueryDTO;
import com.sky.takeout.dto.SetmealUpdateDTO;
import com.sky.takeout.entity.Category;
import com.sky.takeout.entity.Dish;
import com.sky.takeout.entity.Setmeal;
import com.sky.takeout.entity.SetmealDish;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.CategoryMapper;
import com.sky.takeout.mapper.DishMapper;
import com.sky.takeout.mapper.SetmealMapper;
import com.sky.takeout.service.SetmealService;
import com.sky.takeout.vo.PageResult;
import com.sky.takeout.vo.SetmealDetailVO;
import com.sky.takeout.vo.SetmealDishVO;
import com.sky.takeout.vo.SetmealPageVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SetmealServiceImpl implements SetmealService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int SETMEAL_CATEGORY_TYPE = 2;

    private final SetmealMapper setmealMapper;
    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;

    public SetmealServiceImpl(
            SetmealMapper setmealMapper,
            CategoryMapper categoryMapper,
            DishMapper dishMapper
    ) {
        this.setmealMapper = setmealMapper;
        this.categoryMapper = categoryMapper;
        this.dishMapper = dishMapper;
    }

    @Override
    public PageResult<SetmealPageVO> pageQuery(SetmealPageQueryDTO query) {
        normalizeQuery(query);

        long total = setmealMapper.countByQuery(query);
        if (total == 0) {
            return new PageResult<>(0, List.of());
        }

        return new PageResult<>(total, setmealMapper.selectPage(query));
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {CacheNames.SETMEALS, CacheNames.SETMEAL_DETAIL},
            allEntries = true
    )
    public Long create(SetmealCreateDTO setmealCreateDTO) {
        requireSetmealCategory(setmealCreateDTO.getCategoryId());

        String name = setmealCreateDTO.getName().trim();
        validateUniqueName(setmealCreateDTO.getCategoryId(), name, null);
        List<SetmealDish> setmealDishes = buildSetmealDishes(
                null,
                setmealCreateDTO.getDishes()
        );

        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(setmealCreateDTO.getCategoryId());
        setmeal.setName(name);
        setmeal.setPrice(setmealCreateDTO.getPrice());
        setmeal.setStatus(setmealCreateDTO.getStatus());
        setmeal.setDescription(normalizeOptionalText(setmealCreateDTO.getDescription()));
        setmeal.setImage(normalizeOptionalText(setmealCreateDTO.getImage()));
        long currentUserId = CurrentUserContext.getUserIdOrDefault();
        setmeal.setCreateUser(currentUserId);
        setmeal.setUpdateUser(currentUserId);

        try {
            if (setmealMapper.insert(setmeal) != 1) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to create setmeal"
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Setmeal name already exists in this category"
            );
        }

        setmealDishes.forEach(item -> item.setSetmealId(setmeal.getId()));
        insertSetmealDishes(setmealDishes);
        return setmeal.getId();
    }

    @Override
    public SetmealDetailVO getById(Long id) {
        Setmeal setmeal = requireSetmeal(id);
        Category category = categoryMapper.selectById(setmeal.getCategoryId());
        List<SetmealDishVO> dishes = setmealMapper.selectDishesBySetmealId(id);

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
                dishes
        );
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {CacheNames.SETMEALS, CacheNames.SETMEAL_DETAIL},
            allEntries = true
    )
    public void update(SetmealUpdateDTO setmealUpdateDTO) {
        requireSetmeal(setmealUpdateDTO.getId());
        requireSetmealCategory(setmealUpdateDTO.getCategoryId());

        String name = setmealUpdateDTO.getName().trim();
        validateUniqueName(
                setmealUpdateDTO.getCategoryId(),
                name,
                setmealUpdateDTO.getId()
        );
        List<SetmealDish> setmealDishes = buildSetmealDishes(
                setmealUpdateDTO.getId(),
                setmealUpdateDTO.getDishes()
        );

        Setmeal setmeal = new Setmeal();
        setmeal.setId(setmealUpdateDTO.getId());
        setmeal.setCategoryId(setmealUpdateDTO.getCategoryId());
        setmeal.setName(name);
        setmeal.setPrice(setmealUpdateDTO.getPrice());
        setmeal.setDescription(normalizeOptionalText(setmealUpdateDTO.getDescription()));
        setmeal.setImage(normalizeOptionalText(setmealUpdateDTO.getImage()));
        setmeal.setUpdateUser(CurrentUserContext.getUserIdOrDefault());

        try {
            if (setmealMapper.update(setmeal) != 1) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to update setmeal"
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Setmeal name already exists in this category"
            );
        }

        setmealMapper.deleteDishesBySetmealId(setmeal.getId());
        insertSetmealDishes(setmealDishes);
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {CacheNames.SETMEALS, CacheNames.SETMEAL_DETAIL},
            allEntries = true
    )
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Status must be 0 or 1"
            );
        }

        requireSetmeal(id);

        if (setmealMapper.updateStatus(
                id,
                status,
                CurrentUserContext.getUserIdOrDefault()
        ) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to update setmeal status"
            );
        }
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {CacheNames.SETMEALS, CacheNames.SETMEAL_DETAIL},
            allEntries = true
    )
    public void delete(Long id) {
        requireSetmeal(id);

        setmealMapper.deleteDishesBySetmealId(id);
        if (setmealMapper.deleteById(id) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to delete setmeal"
            );
        }
    }

    private void normalizeQuery(SetmealPageQueryDTO query) {
        query.setPage(Math.max(query.getPage(), 1));
        query.setPageSize(Math.min(Math.max(query.getPageSize(), 1), MAX_PAGE_SIZE));

        if (query.getName() != null) {
            String name = query.getName().trim();
            query.setName(name.isEmpty() ? null : name);
        }
    }

    private Setmeal requireSetmeal(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        if (setmeal == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND.value(),
                    "Setmeal not found"
            );
        }
        return setmeal;
    }

    private Category requireSetmealCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Setmeal category not found"
            );
        }

        if (category.getType() != SETMEAL_CATEGORY_TYPE) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Category type must be 2"
            );
        }

        return category;
    }

    private void validateUniqueName(Long categoryId, String name, Long excludeId) {
        if (setmealMapper.countByCategoryAndName(categoryId, name, excludeId) > 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Setmeal name already exists in this category"
            );
        }
    }

    private List<SetmealDish> buildSetmealDishes(
            Long setmealId,
            List<SetmealDishDTO> dishDTOs
    ) {
        Set<Long> dishIds = new HashSet<>();
        List<SetmealDish> setmealDishes = new ArrayList<>(dishDTOs.size());

        for (SetmealDishDTO dishDTO : dishDTOs) {
            if (!dishIds.add(dishDTO.getDishId())) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST.value(),
                        "Setmeal contains duplicate dish IDs"
                );
            }

            Dish dish = dishMapper.selectById(dishDTO.getDishId());
            if (dish == null) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST.value(),
                        "Dish not found: " + dishDTO.getDishId()
                );
            }

            SetmealDish setmealDish = new SetmealDish();
            setmealDish.setSetmealId(setmealId);
            setmealDish.setDishId(dishDTO.getDishId());
            setmealDish.setCopies(dishDTO.getCopies());
            long currentUserId = CurrentUserContext.getUserIdOrDefault();
            setmealDish.setCreateUser(currentUserId);
            setmealDish.setUpdateUser(currentUserId);
            setmealDishes.add(setmealDish);
        }

        return setmealDishes;
    }

    private void insertSetmealDishes(List<SetmealDish> setmealDishes) {
        if (setmealMapper.insertDishes(setmealDishes) != setmealDishes.size()) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create setmeal dishes"
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
