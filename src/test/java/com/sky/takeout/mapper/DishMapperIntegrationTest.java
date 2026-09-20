package com.sky.takeout.mapper;

import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.DishFlavorDTO;
import com.sky.takeout.dto.DishPageQueryDTO;
import com.sky.takeout.dto.DishUpdateDTO;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.service.CategoryService;
import com.sky.takeout.service.DishService;
import com.sky.takeout.vo.DishDetailVO;
import com.sky.takeout.vo.DishPageVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DishMapperIntegrationTest {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Test
    void shouldCompleteDishCrudLifecycleWithFlavors() {
        Long id = dishService.create(createDTO("Spicy Chicken", 1L));

        DishDetailVO created = dishService.getById(id);
        assertEquals("Spicy Chicken", created.getName());
        assertEquals("Hot Dishes", created.getCategoryName());
        assertEquals(new BigDecimal("28.50"), created.getPrice());
        assertEquals(List.of("Spicy", "Mild"), created.getFlavors().get(0).getValue());

        DishPageQueryDTO pageQuery = new DishPageQueryDTO();
        pageQuery.setName("Spicy");
        pageQuery.setCategoryId(1L);
        PageResult<DishPageVO> page = dishService.pageQuery(pageQuery);
        assertEquals(1, page.getTotal());
        assertEquals("Hot Dishes", page.getRecords().get(0).getCategoryName());

        DishUpdateDTO updateDTO = updateDTO(id);
        dishService.update(updateDTO);
        dishService.updateStatus(id, 1);

        DishDetailVO updated = dishService.getById(id);
        assertEquals("Updated Chicken", updated.getName());
        assertEquals(1, updated.getStatus());
        assertEquals(List.of("Mild"), updated.getFlavors().get(0).getValue());

        dishService.delete(id);
    }

    @Test
    void shouldRejectSetMealCategoryForDish() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> dishService.create(createDTO("Invalid Dish", 2L))
        );

        assertEquals(400, exception.getCode());
        assertEquals("Category type must be 1", exception.getMessage());
    }

    @Test
    void shouldPreventDeletingCategoryReferencedByDish() {
        dishService.create(createDTO("Referenced Dish", 1L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.delete(1L)
        );

        assertEquals(409, exception.getCode());
        assertEquals("Category is referenced by dishes", exception.getMessage());
    }

    private DishCreateDTO createDTO(String name, Long categoryId) {
        DishCreateDTO createDTO = new DishCreateDTO();
        createDTO.setName(name);
        createDTO.setCategoryId(categoryId);
        createDTO.setPrice(new BigDecimal("28.50"));
        createDTO.setImage("/images/dish.jpg");
        createDTO.setDescription("Test dish");
        createDTO.setStatus(0);

        DishFlavorDTO flavorDTO = new DishFlavorDTO();
        flavorDTO.setName("Spiciness");
        flavorDTO.setValue(List.of("Spicy", "Mild"));
        createDTO.setFlavors(List.of(flavorDTO));
        return createDTO;
    }

    private DishUpdateDTO updateDTO(Long id) {
        DishUpdateDTO updateDTO = new DishUpdateDTO();
        updateDTO.setId(id);
        updateDTO.setName("Updated Chicken");
        updateDTO.setCategoryId(1L);
        updateDTO.setPrice(new BigDecimal("30.00"));
        updateDTO.setDescription("Updated dish");

        DishFlavorDTO flavorDTO = new DishFlavorDTO();
        flavorDTO.setName("Spiciness");
        flavorDTO.setValue(List.of("Mild"));
        updateDTO.setFlavors(List.of(flavorDTO));
        return updateDTO;
    }
}
