package com.sky.takeout.interceptor;

import jakarta.servlet.http.HttpServletRequest;

final class AuthTokenResolver {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_HEADER = "token";
    private static final String BEARER_PREFIX = "Bearer ";

    private AuthTokenResolver() {
    }

    static String resolve(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            String token = authorization.substring(BEARER_PREFIX.length()).trim();
            return token.isEmpty() ? null : token;
        }

        String token = request.getHeader(TOKEN_HEADER);
        return token == null || token.isBlank() ? null : token.trim();
    }
}
