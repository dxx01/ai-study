package com.example.ai.auth.web.dto;

import java.util.List;

/**
 * 当前登录用户信息。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
public record UserInfoResponse(
        String loginId,
        String username,
        List<String> roles,
        List<String> permissions
) {
}
