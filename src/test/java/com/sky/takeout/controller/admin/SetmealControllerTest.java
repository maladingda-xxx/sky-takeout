package com.sky.takeout.controller.admin;

import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.service.SetmealService;
import com.sky.takeout.vo.SetmealDetailVO;
import com.sky.takeout.vo.SetmealDishVO;
import com.sky.takeout.vo.SetmealPageVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(SetmealController.class)
class SetmealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SetmealService setmealService;

    @Test
    void shouldReturnSetmealPage() throws Exception {
        SetmealPageVO setmeal = new SetmealPageVO();
        setmeal.setId(1L);
        setmeal.setCategoryId(2L);
        setmeal.setCategoryName("Set Meals");
        setmeal.setName("Family Meal");
        setmeal.setPrice(new BigDecimal("68.00"));
        setmeal.setStatus(1);

        when(setmealService.pageQuery(any()))
                .thenReturn(new PageResult<>(1, List.of(setmeal)));

        mockMvc.perform(get("/admin/setmeal/page")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].categoryName")
                        .value("Set Meals"));
    }

    @Test
    void shouldCreateSetmeal() throws Exception {
        when(setmealService.create(any())).thenReturn(5L);

        mockMvc.perform(post("/admin/setmeal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 2,
                                  "name": "Family Meal",
                                  "price": 68.00,
                                  "status": 0,
                                  "dishes": [
                                    {"dishId": 10, "copies": 1},
                                    {"dishId": 11, "copies": 2}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void shouldRejectInvalidSetmealCreateRequest() throws Exception {
        mockMvc.perform(post("/admin/setmeal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 0,
                                  "name": "",
                                  "price": -1,
                                  "status": 2,
                                  "dishes": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldGetSetmealDetail() throws Exception {
        when(setmealService.getById(1L)).thenReturn(new SetmealDetailVO(
                1L,
                2L,
                "Set Meals",
                "Family Meal",
                new BigDecimal("68.00"),
                1,
                "Dinner for four",
                "/images/family-meal.jpg",
                LocalDateTime.of(2026, 9, 20, 13, 0),
                List.of(new SetmealDishVO(
                        10L,
                        "Spicy Chicken",
                        new BigDecimal("28.50"),
                        2
                ))
        ));

        mockMvc.perform(get("/admin/setmeal/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryName").value("Set Meals"))
                .andExpect(jsonPath("$.data.dishes[0].dishName")
                        .value("Spicy Chicken"));
    }

    @Test
    void shouldReturnNotFoundForMissingSetmeal() throws Exception {
        when(setmealService.getById(99L))
                .thenThrow(new BusinessException(404, "Setmeal not found"));

        mockMvc.perform(get("/admin/setmeal/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Setmeal not found"));
    }

    @Test
    void shouldUpdateChangeStatusAndDeleteSetmeal() throws Exception {
        doNothing().when(setmealService).update(any());
        doNothing().when(setmealService).updateStatus(1L, 1);
        doNothing().when(setmealService).delete(1L);

        mockMvc.perform(put("/admin/setmeal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "categoryId": 2,
                                  "name": "Updated Meal",
                                  "price": 72.00,
                                  "description": "Updated",
                                  "dishes": [
                                    {"dishId": 10, "copies": 2}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/admin/setmeal/status/1")
                        .param("id", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/admin/setmeal/1"))
                .andExpect(status().isOk());
    }
}
