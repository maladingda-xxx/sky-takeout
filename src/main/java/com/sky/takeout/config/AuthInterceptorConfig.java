package com.sky.takeout.config;

import com.sky.takeout.interceptor.LoginInterceptor;
import com.sky.takeout.utils.JwtUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnBean(JwtUtil.class)
@ConditionalOnProperty(
        name = "sky.auth.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AuthInterceptorConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;
    private final AuthProperties authProperties;

    public AuthInterceptorConfig(
            JwtUtil jwtUtil,
            AuthProperties authProperties
    ) {
        this.jwtUtil = jwtUtil;
        this.authProperties = authProperties;
    }

    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor(jwtUtil, authProperties);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");
    }
}
