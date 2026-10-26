package com.example.ai.auth.web.dto;

import com.example.ai.common.api.dto.MenuItem;

import java.util.List;

/**
 * 登录成功响应（token 同时写入 Sa-Token 响应头 Authorization）。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
public record LoginResponse(
        String token,
        String tokenType,
        String loginId,
        String username,
        List<String> roles,
        List<String> permissions,
        List<MenuItem> menus
) {
}
