package com.sky.takeout.controller.user;

import com.sky.takeout.service.UserCatalogService;
import com.sky.takeout.vo.CategoryVO;
import com.sky.takeout.vo.DishUserVO;
import com.sky.takeout.vo.SetmealPageVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserCatalogController.class)
class UserCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCatalogService userCatalogService;

    @Test
    void shouldListEnabledCategories() throws Exception {
        when(userCatalogService.listCategories(1)).thenReturn(List.of(
                new CategoryVO(1L, 1, "Hot Dishes", 10, 1,
                        LocalDateTime.of(2026, 9, 20, 10, 0))
        ));

        mockMvc.perform(get("/user/category/list").param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Hot Dishes"));
    }

    @Test
    void shouldListEnabledDishes() throws Exception {
        when(userCatalogService.listDishes(1L)).thenReturn(List.of(
                new DishUserVO(
                        10L,
                        "Spicy Chicken",
                        new BigDecimal("28.50"),
                        "/dish.jpg",
                        "Spicy",
                        List.of()
                )
        ));

        mockMvc.perform(get("/user/dish/list").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Spicy Chicken"));
    }

    @Test
    void shouldListEnabledSetmeals() throws Exception {
        SetmealPageVO setmeal = new SetmealPageVO();
        setmeal.setId(5L);
        setmeal.setCategoryId(2L);
        setmeal.setCategoryName("Set Meals");
        setmeal.setName("Family Meal");
        setmeal.setPrice(new BigDecimal("68.00"));
        setmeal.setStatus(1);
        when(userCatalogService.listSetmeals(2L)).thenReturn(List.of(setmeal));

        mockMvc.perform(get("/user/setmeal/list").param("categoryId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Family Meal"));
    }
}
