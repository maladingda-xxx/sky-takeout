package com.sky.takeout.controller.admin;

import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.service.CategoryService;
import com.sky.takeout.vo.CategoryVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldReturnWrappedPageResult() throws Exception {
        CategoryVO category = categoryVO(1L, 1, "Hot Dishes");
        when(categoryService.pageQuery(any()))
                .thenReturn(new PageResult<>(1, List.of(category)));

        mockMvc.perform(get("/admin/category/page")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("Hot Dishes"));
    }

    @Test
    void shouldCreateCategory() throws Exception {
        when(categoryService.create(any())).thenReturn(3L);

        mockMvc.perform(post("/admin/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": 1,
                                  "name": "New Category",
                                  "sort": 20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    void shouldRejectInvalidCategoryCreateRequest() throws Exception {
        mockMvc.perform(post("/admin/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": 3,
                                  "name": "",
                                  "sort": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldListCategoriesByType() throws Exception {
        when(categoryService.listByType(2)).thenReturn(List.of(
                categoryVO(2L, 2, "Set Meals")
        ));

        mockMvc.perform(get("/admin/category/list")
                        .param("type", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Set Meals"));
    }

    @Test
    void shouldReturnNotFoundForMissingCategory() throws Exception {
        when(categoryService.getById(99L))
                .thenThrow(new BusinessException(404, "Category not found"));

        mockMvc.perform(get("/admin/category/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    void shouldUpdateChangeStatusAndDeleteCategory() throws Exception {
        doNothing().when(categoryService).update(any());
        doNothing().when(categoryService).updateStatus(1L, 0);
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(put("/admin/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "type": 1,
                                  "name": "Updated Category",
                                  "sort": 5
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/admin/category/status/0")
                        .param("id", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/admin/category/1"))
                .andExpect(status().isOk());
    }

    private CategoryVO categoryVO(Long id, Integer type, String name) {
        return new CategoryVO(
                id,
                type,
                name,
                10,
                1,
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );
    }
}
