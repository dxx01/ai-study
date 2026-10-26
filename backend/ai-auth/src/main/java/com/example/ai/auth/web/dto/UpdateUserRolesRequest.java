package com.example.ai.auth.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 修改用户角色。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
public record UpdateUserRolesRequest(
        @NotNull List<String> roles
) {
}
