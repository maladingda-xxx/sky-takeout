package com.sky.takeout.service.impl;

import com.sky.takeout.common.CurrentUserContext;
import com.sky.takeout.common.CacheNames;
import com.sky.takeout.dto.CategoryCreateDTO;
import com.sky.takeout.dto.CategoryPageQueryDTO;
import com.sky.takeout.dto.CategoryUpdateDTO;
import com.sky.takeout.entity.Category;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.CategoryMapper;
import com.sky.takeout.service.CategoryService;
import com.sky.takeout.vo.CategoryVO;
import com.sky.takeout.vo.PageResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int ENABLED_STATUS = 1;

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public PageResult<CategoryVO> pageQuery(CategoryPageQueryDTO query) {
        normalizeQuery(query);

        long total = categoryMapper.countByQuery(query);
        if (total == 0) {
            return new PageResult<>(0, List.of());
        }

        List<CategoryVO> records = categoryMapper.selectPage(query)
                .stream()
                .map(this::toVO)
                .toList();

        return new PageResult<>(total, records);
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    CacheNames.CATEGORIES,
                    CacheNames.DISHES,
                    CacheNames.SETMEALS,
                    CacheNames.SETMEAL_DETAIL,
                    CacheNames.CATALOG_MISS
            },
            allEntries = true
    )
    public Long create(CategoryCreateDTO categoryCreateDTO) {
        String name = categoryCreateDTO.getName().trim();
        validateUnique(categoryCreateDTO.getType(), name, null);

        Category category = new Category();
        category.setType(categoryCreateDTO.getType());
        category.setName(name);
        category.setSort(categoryCreateDTO.getSort());
        category.setStatus(ENABLED_STATUS);
        long currentUserId = CurrentUserContext.getUserIdOrDefault();
        category.setCreateUser(currentUserId);
        category.setUpdateUser(currentUserId);

        try {
            if (categoryMapper.insert(category) != 1) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to create category"
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Category name already exists for this type"
            );
        }

        return category.getId();
    }

    @Override
    public CategoryVO getById(Long id) {
        return toVO(requireCategory(id));
    }

    @Override
    public List<CategoryVO> listByType(Integer type) {
        if (type == null || (type != 1 && type != 2)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Type must be 1 or 2"
            );
        }

        return categoryMapper.selectByType(type)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    CacheNames.CATEGORIES,
                    CacheNames.DISHES,
                    CacheNames.SETMEALS,
                    CacheNames.SETMEAL_DETAIL,
                    CacheNames.CATALOG_MISS
            },
            allEntries = true
    )
    public void update(CategoryUpdateDTO categoryUpdateDTO) {
        requireCategory(categoryUpdateDTO.getId());

        String name = categoryUpdateDTO.getName().trim();
        validateUnique(
                categoryUpdateDTO.getType(),
                name,
                categoryUpdateDTO.getId()
        );

        Category category = new Category();
        category.setId(categoryUpdateDTO.getId());
        category.setType(categoryUpdateDTO.getType());
        category.setName(name);
        category.setSort(categoryUpdateDTO.getSort());
        category.setUpdateUser(CurrentUserContext.getUserIdOrDefault());

        try {
            if (categoryMapper.update(category) != 1) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to update category"
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Category name already exists for this type"
            );
        }
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    CacheNames.CATEGORIES,
                    CacheNames.DISHES,
                    CacheNames.SETMEALS,
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

        requireCategory(id);

        if (categoryMapper.updateStatus(
                id,
                status,
                CurrentUserContext.getUserIdOrDefault()
        ) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to update category status"
            );
        }
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    CacheNames.CATEGORIES,
                    CacheNames.DISHES,
                    CacheNames.SETMEALS,
                    CacheNames.SETMEAL_DETAIL,
                    CacheNames.CATALOG_MISS
            },
            allEntries = true
    )
    public void delete(Long id) {
        requireCategory(id);

        if (categoryMapper.countDishesByCategoryId(id) > 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Category is referenced by dishes"
            );
        }

        if (categoryMapper.countSetmealsByCategoryId(id) > 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Category is referenced by setmeals"
            );
        }

        if (categoryMapper.deleteById(id) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to delete category"
            );
        }
    }

    private void normalizeQuery(CategoryPageQueryDTO query) {
        query.setPage(Math.max(query.getPage(), 1));
        query.setPageSize(Math.min(Math.max(query.getPageSize(), 1), MAX_PAGE_SIZE));

        if (query.getName() != null) {
            String name = query.getName().trim();
            query.setName(name.isEmpty() ? null : name);
        }
    }

    private Category requireCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND.value(),
                    "Category not found"
            );
        }
        return category;
    }

    private void validateUnique(Integer type, String name, Long excludeId) {
        if (categoryMapper.countByTypeAndName(type, name, excludeId) > 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Category name already exists for this type"
            );
        }
    }

    private CategoryVO toVO(Category category) {
        return new CategoryVO(
                category.getId(),
                category.getType(),
                category.getName(),
                category.getSort(),
                category.getStatus(),
                category.getUpdateTime()
        );
    }
}
