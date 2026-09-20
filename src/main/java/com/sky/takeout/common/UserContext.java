package com.sky.takeout.common;

import com.sky.takeout.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getRequiredUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unauthorized"
            );
        }
        return userId;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
