package com.example.ai.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册请求。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
public class RegisterRequest {

    @NotBlank(message = "username 不能为空")
    @Size(min = 3, max = 32, message = "用户名长度 3~32")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名仅允许字母数字下划线")
    private String username;

    @NotBlank(message = "password 不能为空")
    @Size(min = 6, max = 128, message = "密码至少 6 位")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
