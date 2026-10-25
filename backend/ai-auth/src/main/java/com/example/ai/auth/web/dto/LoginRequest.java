package com.example.ai.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
public record LoginRequest(
        @NotBlank(message = "username 不能为空")
        @Size(max = 64)
        String username,
        @NotBlank(message = "password 不能为空")
        @Size(max = 128)
        String password
) {
}
