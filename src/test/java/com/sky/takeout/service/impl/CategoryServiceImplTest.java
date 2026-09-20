package com.sky.takeout.service.impl;

import com.sky.takeout.dto.CategoryCreateDTO;
import com.sky.takeout.dto.CategoryPageQueryDTO;
import com.sky.takeout.dto.CategoryUpdateDTO;
import com.sky.takeout.entity.Category;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.CategoryMapper;
import com.sky.takeout.vo.CategoryVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void shouldReturnEmptyPageWithoutSelectingRows() {
        CategoryPageQueryDTO query = new CategoryPageQueryDTO();
        when(categoryMapper.countByQuery(query)).thenReturn(0L);

        PageResult<CategoryVO> result = categoryService.pageQuery(query);

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(categoryMapper, never()).selectPage(query);
    }

    @Test
    void shouldNormalizeQueryAndMapCategoryToVO() {
        CategoryPageQueryDTO query = new CategoryPageQueryDTO();
        query.setPage(0);
        query.setPageSize(500);
        query.setName("  Hot  ");

        Category category = category(1L, 1, "Hot Dishes", 10, 1);
        when(categoryMapper.countByQuery(query)).thenReturn(1L);
        when(categoryMapper.selectPage(query)).thenReturn(List.of(category));

        PageResult<CategoryVO> result = categoryService.pageQuery(query);

        ArgumentCaptor<CategoryPageQueryDTO> captor =
                ArgumentCaptor.forClass(CategoryPageQueryDTO.class);
        verify(categoryMapper).countByQuery(captor.capture());

        CategoryPageQueryDTO normalizedQuery = captor.getValue();
        assertEquals(1, normalizedQuery.getPage());
        assertEquals(100, normalizedQuery.getPageSize());
        assertEquals("Hot", normalizedQuery.getName());
        assertEquals("Hot Dishes", result.getRecords().get(0).getName());
    }

    @Test
    void shouldCreateEnabledCategory() {
        CategoryCreateDTO createDTO = new CategoryCreateDTO();
        createDTO.setType(1);
        createDTO.setName("  New Category  ");
        createDTO.setSort(20);

        when(categoryMapper.countByTypeAndName(1, "New Category", null))
                .thenReturn(0L);
        when(categoryMapper.insert(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(3L);
            return 1;
        });

        Long id = categoryService.create(createDTO);

        assertEquals(3L, id);
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryMapper).insert(captor.capture());

        Category category = captor.getValue();
        assertEquals(1, category.getType());
        assertEquals("New Category", category.getName());
        assertEquals(20, category.getSort());
        assertEquals(1, category.getStatus());
        assertEquals(1L, category.getCreateUser());
    }

    @Test
    void shouldRejectDuplicateCategoryNameWithinType() {
        CategoryCreateDTO createDTO = new CategoryCreateDTO();
        createDTO.setType(1);
        createDTO.setName("Hot Dishes");
        createDTO.setSort(10);

        when(categoryMapper.countByTypeAndName(1, "Hot Dishes", null))
                .thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.create(createDTO)
        );

        assertEquals(409, exception.getCode());
        verify(categoryMapper, never()).insert(any(Category.class));
    }

    @Test
    void shouldRejectUpdateWhenCategoryDoesNotExist() {
        CategoryUpdateDTO updateDTO = new CategoryUpdateDTO();
        updateDTO.setId(99L);
        when(categoryMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.update(updateDTO)
        );

        assertEquals(404, exception.getCode());
        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidTypeForCategoryList() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.listByType(3)
        );

        assertEquals(400, exception.getCode());
        assertEquals("Type must be 1 or 2", exception.getMessage());
    }

    private Category category(Long id, Integer type, String name, Integer sort, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setType(type);
        category.setName(name);
        category.setSort(sort);
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.of(2026, 9, 20, 10, 0));
        return category;
    }
}
