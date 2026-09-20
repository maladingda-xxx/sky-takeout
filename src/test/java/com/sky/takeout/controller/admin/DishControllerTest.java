package com.sky.takeout.controller.admin;

import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.service.DishService;
import com.sky.takeout.vo.DishDetailVO;
import com.sky.takeout.vo.DishFlavorVO;
import com.sky.takeout.vo.DishPageVO;
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

@WebMvcTest(DishController.class)
class DishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DishService dishService;

    @Test
    void shouldReturnDishPage() throws Exception {
        DishPageVO dish = new DishPageVO();
        dish.setId(1L);
        dish.setName("Spicy Chicken");
        dish.setCategoryId(1L);
        dish.setCategoryName("Hot Dishes");
        dish.setPrice(new BigDecimal("28.50"));
        dish.setStatus(1);

        when(dishService.pageQuery(any()))
                .thenReturn(new PageResult<>(1, List.of(dish)));

        mockMvc.perform(get("/admin/dish/page")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].categoryName")
                        .value("Hot Dishes"));
    }

    @Test
    void shouldCreateDish() throws Exception {
        when(dishService.create(any())).thenReturn(10L);

        mockMvc.perform(post("/admin/dish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Spicy Chicken",
                                  "categoryId": 1,
                                  "price": 28.50,
                                  "status": 0,
                                  "flavors": [
                                    {
                                      "name": "Spiciness",
                                      "value": ["Spicy", "Mild"]
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    void shouldRejectInvalidDishCreateRequest() throws Exception {
        mockMvc.perform(post("/admin/dish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "categoryId": 0,
                                  "price": -1,
                                  "status": 2,
                                  "flavors": [
                                    {
                                      "name": "",
                                      "value": []
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldGetDishDetail() throws Exception {
        when(dishService.getById(1L)).thenReturn(new DishDetailVO(
                1L,
                "Spicy Chicken",
                1L,
                "Hot Dishes",
                new BigDecimal("28.50"),
                "/images/spicy-chicken.jpg",
                "Spicy chicken",
                1,
                LocalDateTime.of(2026, 9, 20, 12, 0),
                List.of(new DishFlavorVO("Spiciness", List.of("Spicy", "Mild")))
        ));

        mockMvc.perform(get("/admin/dish/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryName").value("Hot Dishes"))
                .andExpect(jsonPath("$.data.flavors[0].value[0]").value("Spicy"));
    }

    @Test
    void shouldReturnNotFoundForMissingDish() throws Exception {
        when(dishService.getById(99L))
                .thenThrow(new BusinessException(404, "Dish not found"));

        mockMvc.perform(get("/admin/dish/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dish not found"));
    }

    @Test
    void shouldUpdateChangeStatusAndDeleteDish() throws Exception {
        doNothing().when(dishService).update(any());
        doNothing().when(dishService).updateStatus(1L, 1);
        doNothing().when(dishService).delete(1L);

        mockMvc.perform(put("/admin/dish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "name": "Updated Chicken",
                                  "categoryId": 1,
                                  "price": 30.00,
                                  "description": "Updated",
                                  "flavors": []
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/admin/dish/status/1")
                        .param("id", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/admin/dish/1"))
                .andExpect(status().isOk());
    }
}
