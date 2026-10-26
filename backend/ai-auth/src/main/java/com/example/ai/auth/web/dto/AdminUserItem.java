package com.example.ai.auth.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端用户列表项（不含密码）。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
public record AdminUserItem(
        Long id,
        String username,
        Integer status,
        List<String> roles,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}
