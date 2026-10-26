package com.example.ai.auth.web.dto;

/**
 * 权限选项。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
public record AdminPermissionItem(
        Long id,
        String code,
        String name,
        String groupName
) {
}
