package com.example.ai.common.security;

/**
 * 演示账号明文密码（仅本地 PoC；生产必须使用数据库与密钥托管）。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
public final class DemoPasswords {

    private DemoPasswords() {
    }

    public static final String ADMIN_PASSWORD = "admin123";
    public static final String USER_PASSWORD = "user123";
}
