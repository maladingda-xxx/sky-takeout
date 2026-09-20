package com.sky.takeout.controller.user;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.service.UserService;
import com.sky.takeout.vo.UserLoginVO;
import com.sky.takeout.vo.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void shouldLoginUser() throws Exception {
        when(userService.login(any())).thenReturn(
                new UserLoginVO(7L, "openid-7", "user-token")
        );

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "valid-code"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.token").value("user-token"));
    }

    @Test
    void shouldReturnUserProfile() throws Exception {
        UserContext.setUserId(7L);
        when(userService.getById(7L)).thenReturn(
                new UserVO(7L, "WeChat User", "13800000000", 1, "/avatar.jpg")
        );

        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.name").value("WeChat User"));
    }
}
