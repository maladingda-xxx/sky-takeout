package com.sky.takeout.controller.user;

import com.sky.takeout.service.ShoppingCartService;
import com.sky.takeout.vo.ShoppingCartVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShoppingCartController.class)
class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShoppingCartService shoppingCartService;

    @Test
    void shouldAddCartItem() throws Exception {
        doNothing().when(shoppingCartService).add(any());

        mockMvc.perform(post("/user/shoppingCart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dishId": 10,
                                  "quantity": 2,
                                  "flavor": "Mild"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldListCartItems() throws Exception {
        ShoppingCartVO item = new ShoppingCartVO();
        item.setId(1L);
        item.setProductId(10L);
        item.setProductType("dish");
        item.setName("Spicy Chicken");
        item.setUnitPrice(new BigDecimal("28.50"));
        item.setQuantity(2);
        item.setAmount(new BigDecimal("57.00"));
        when(shoppingCartService.list()).thenReturn(List.of(item));

        mockMvc.perform(get("/user/shoppingCart/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Spicy Chicken"))
                .andExpect(jsonPath("$.data[0].amount").value(57.00));
    }

    @Test
    void shouldUpdateSubtractAndCleanCart() throws Exception {
        doNothing().when(shoppingCartService).sub(any());
        doNothing().when(shoppingCartService).updateQuantity(any());
        doNothing().when(shoppingCartService).clean();

        mockMvc.perform(post("/user/shoppingCart/sub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dishId": 10}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/user/shoppingCart/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": 1, "quantity": 5}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/user/shoppingCart/clean"))
                .andExpect(status().isOk());
    }
}
