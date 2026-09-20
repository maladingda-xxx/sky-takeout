package com.sky.takeout.interceptor;

import com.sky.takeout.common.CurrentUserContext;
import com.sky.takeout.config.AuthProperties;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final AuthProperties authProperties;

    public LoginInterceptor(JwtUtil jwtUtil, AuthProperties authProperties) {
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
            CurrentUserContext.setUserId(jwtUtil.parseEmployeeId(token));
            return true;
        } catch (RuntimeException exception) {
            CurrentUserContext.clear();
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
        CurrentUserContext.clear();
    }

    private BusinessException unauthorized() {
        return new BusinessException(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized"
        );
    }
}
