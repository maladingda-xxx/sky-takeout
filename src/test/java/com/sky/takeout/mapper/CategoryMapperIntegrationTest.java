package com.sky.takeout.mapper;

import com.sky.takeout.dto.CategoryCreateDTO;
import com.sky.takeout.dto.CategoryPageQueryDTO;
import com.sky.takeout.dto.CategoryUpdateDTO;
import com.sky.takeout.entity.Category;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.service.CategoryService;
import com.sky.takeout.vo.CategoryVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryMapperIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    void shouldFilterAndPageCategories() {
        CategoryPageQueryDTO query = new CategoryPageQueryDTO();
        query.setType(1);
        query.setName("Hot");

        PageResult<CategoryVO> result = categoryService.pageQuery(query);

        assertEquals(1, result.getTotal());
        assertEquals("Hot Dishes", result.getRecords().get(0).getName());
    }

    @Test
    void shouldCompleteCategoryCrudLifecycle() {
        Long id = categoryService.create(createDTO(1, "Desserts", 20));

        Category created = categoryMapper.selectById(id);
        assertEquals(1, created.getType());
        assertEquals("Desserts", created.getName());
        assertEquals(20, created.getSort());
        assertEquals(1, created.getStatus());

        CategoryUpdateDTO updateDTO = new CategoryUpdateDTO();
        updateDTO.setId(id);
        updateDTO.setType(1);
        updateDTO.setName("Sweet Desserts");
        updateDTO.setSort(5);

        categoryService.update(updateDTO);
        categoryService.updateStatus(id, 0);

        CategoryVO updated = categoryService.getById(id);
        assertEquals("Sweet Desserts", updated.getName());
        assertEquals(5, updated.getSort());
        assertEquals(0, updated.getStatus());

        categoryService.delete(id);

        assertNull(categoryMapper.selectById(id));
    }

    @Test
    void shouldRejectDuplicateNameWithinSameTypeButAllowAcrossTypes() {
        categoryService.create(createDTO(1, "Beverages", 30));

        BusinessException duplicateException = assertThrows(
                BusinessException.class,
                () -> categoryService.create(createDTO(1, "Beverages", 30))
        );
        assertEquals(409, duplicateException.getCode());

        Long setMealCategoryId = categoryService.create(
                createDTO(2, "Beverages", 30)
        );
        assertEquals(2, categoryService.getById(setMealCategoryId).getType());
    }

    @Test
    void shouldListCategoriesInSortOrder() {
        categoryService.create(createDTO(1, "Later", 50));
        categoryService.create(createDTO(1, "Earlier", 1));

        List<CategoryVO> categories = categoryService.listByType(1);

        assertEquals("Earlier", categories.get(0).getName());
        assertEquals("Hot Dishes", categories.get(1).getName());
        assertEquals("Later", categories.get(2).getName());
    }

    private CategoryCreateDTO createDTO(Integer type, String name, Integer sort) {
        CategoryCreateDTO createDTO = new CategoryCreateDTO();
        createDTO.setType(type);
        createDTO.setName(name);
        createDTO.setSort(sort);
        return createDTO;
    }
}
