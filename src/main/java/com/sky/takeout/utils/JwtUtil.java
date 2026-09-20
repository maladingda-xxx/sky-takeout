package com.sky.takeout.utils;

import com.sky.takeout.common.JwtClaims;
import com.sky.takeout.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String CLAIM_SUBJECT_TYPE = "subjectType";
    private static final String EMPLOYEE_SUBJECT = "employee";
    private static final String USER_SUBJECT = "user";

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(Long employeeId) {
        return generateToken(employeeId, EMPLOYEE_SUBJECT);
    }

    public String generateUserToken(Long userId) {
        return generateToken(userId, USER_SUBJECT);
    }

    private String generateToken(Long subjectId, String subjectType) {
        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + jwtProperties.getExpirationMs()
        );

        return Jwts.builder()
                .subject(String.valueOf(subjectId))
                .claim(CLAIM_SUBJECT_TYPE, subjectType)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public Long parseEmployeeId(String token) {
        JwtClaims claims = parseToken(token);
        if (!EMPLOYEE_SUBJECT.equals(claims.subjectType())) {
            throw new JwtException("Token is not an employee token");
        }
        return claims.subjectId();
    }

    public Long parseUserId(String token) {
        JwtClaims claims = parseToken(token);
        if (!USER_SUBJECT.equals(claims.subjectType())) {
            throw new JwtException("Token is not a user token");
        }
        return claims.subjectId();
    }

    public JwtClaims parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subjectType = claims.get(CLAIM_SUBJECT_TYPE, String.class);
        if (subjectType == null || subjectType.isBlank()) {
            subjectType = EMPLOYEE_SUBJECT;
        }

        return new JwtClaims(
                Long.valueOf(claims.getSubject()),
                subjectType
        );
    }
}
