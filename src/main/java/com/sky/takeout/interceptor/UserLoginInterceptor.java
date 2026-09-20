package com.sky.takeout.interceptor;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.config.AuthProperties;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class UserLoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final AuthProperties authProperties;

    public UserLoginInterceptor(JwtUtil jwtUtil, AuthProperties authProperties) {
        this.jwtUtil = jwtUtil;
        this.authProperties = authProperties;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (!authProperties.isEnabled()) {
            return true;
        }

        String token = AuthTokenResolver.resolve(request);
        if (token == null) {
            throw unauthorized();
        }

        try {
            UserContext.setUserId(jwtUtil.parseUserId(token));
            return true;
        } catch (RuntimeException exception) {
            UserContext.clear();
            throw unauthorized();
        }
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception
    ) {
        UserContext.clear();
    }

    private BusinessException unauthorized() {
        return new BusinessException(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized"
        );
    }
}
