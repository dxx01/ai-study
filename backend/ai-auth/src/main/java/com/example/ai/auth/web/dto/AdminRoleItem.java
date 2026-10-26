package com.example.ai.auth.web.dto;

import java.util.List;

/**
 * 管理端角色列表项。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
public record AdminRoleItem(
        Long id,
        String code,
        String name,
        Integer status,
        Integer sortOrder,
        List<String> permissions
) {
}
