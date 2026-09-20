package com.sky.takeout.service.impl;

import com.sky.takeout.dto.UserLoginDTO;
import com.sky.takeout.entity.UserAccount;
import com.sky.takeout.mapper.UserAccountMapper;
import com.sky.takeout.service.WeChatAuthService;
import com.sky.takeout.utils.JwtUtil;
import com.sky.takeout.vo.UserLoginVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private WeChatAuthService weChatAuthService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldLoginExistingUser() {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setCode("valid-code");

        UserAccount user = new UserAccount();
        user.setId(7L);
        user.setOpenid("openid-7");

        when(weChatAuthService.exchangeCode("valid-code"))
                .thenReturn(new WeChatAuthService.WeChatSession(
                        "openid-7",
                        "session-key"
                ));
        when(userAccountMapper.selectByOpenid("openid-7")).thenReturn(user);
        when(jwtUtil.generateUserToken(7L)).thenReturn("user-token");

        UserLoginVO result = userService.login(loginDTO);

        assertEquals(7L, result.getId());
        assertEquals("openid-7", result.getOpenid());
        assertEquals("user-token", result.getToken());
    }

    @Test
    void shouldCreateUserOnFirstLogin() {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setCode("new-code");

        when(weChatAuthService.exchangeCode("new-code"))
                .thenReturn(new WeChatAuthService.WeChatSession(
                        "openid-new",
                        "session-key"
                ));
        when(userAccountMapper.selectByOpenid("openid-new")).thenReturn(null);
        when(userAccountMapper.insert(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount user = invocation.getArgument(0);
            user.setId(8L);
            return 1;
        });
        when(jwtUtil.generateUserToken(8L)).thenReturn("new-user-token");

        UserLoginVO result = userService.login(loginDTO);

        assertEquals(8L, result.getId());
        assertEquals("openid-new", result.getOpenid());
        verify(userAccountMapper).insert(any(UserAccount.class));
    }
}
