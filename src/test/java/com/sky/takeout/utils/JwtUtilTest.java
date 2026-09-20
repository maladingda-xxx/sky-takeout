package com.sky.takeout.utils;

import com.sky.takeout.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    @Test
    void shouldGenerateAndParseToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-must-be-at-least-thirty-two-characters");
        properties.setExpirationMs(60000);
        JwtUtil jwtUtil = new JwtUtil(properties);

        String token = jwtUtil.generateToken(42L);

        assertEquals(42L, jwtUtil.parseEmployeeId(token));
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-must-be-at-least-thirty-two-characters");
        properties.setExpirationMs(-1000);
        JwtUtil jwtUtil = new JwtUtil(properties);

        String token = jwtUtil.generateToken(42L);

        assertThrows(JwtException.class, () -> jwtUtil.parseEmployeeId(token));
    }

    @Test
    void shouldSeparateEmployeeAndUserTokens() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-must-be-at-least-thirty-two-characters");
        properties.setExpirationMs(60000);
        JwtUtil jwtUtil = new JwtUtil(properties);

        String employeeToken = jwtUtil.generateToken(1L);
        String userToken = jwtUtil.generateUserToken(7L);

        assertEquals(1L, jwtUtil.parseEmployeeId(employeeToken));
        assertEquals(7L, jwtUtil.parseUserId(userToken));
        assertThrows(JwtException.class, () -> jwtUtil.parseUserId(employeeToken));
        assertThrows(JwtException.class, () -> jwtUtil.parseEmployeeId(userToken));
    }
}
