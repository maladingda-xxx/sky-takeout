package com.sky.takeout.common;

public final class CurrentUserContext {

    private static final long DEFAULT_USER_ID = 1L;
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static long getUserIdOrDefault() {
        Long userId = USER_ID.get();
        return userId == null ? DEFAULT_USER_ID : userId;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
