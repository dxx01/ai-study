package com.example.ai.auth.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 修改角色权限。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
public record UpdateRolePermissionsRequest(
        @NotNull List<String> permissions
) {
}
