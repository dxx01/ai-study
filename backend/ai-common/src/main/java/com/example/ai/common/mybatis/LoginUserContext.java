package com.example.ai.common.mybatis;

import java.util.Optional;

/**
 * 当前请求登录用户 ID（ThreadLocal），供 MyBatis-Plus 自动填充 owner_user_id 等字段。
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
public final class LoginUserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private LoginUserContext() {
    }

    /**
     * @param userId 当前用户主键
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * @return 当前用户 ID
     */
    public static Optional<Long> getUserIdOptional() {
        return Optional.ofNullable(USER_ID.get());
    }

    /**
     * 请求结束必须清理，避免线程池复用泄漏。
     */
    public static void clear() {
        USER_ID.remove();
    }
}
