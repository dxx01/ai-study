package com.example.ai.common.api.dto;

import java.util.List;

/**
 * 菜单项（前端导航）。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
public record MenuItem(
        Long id,
        Long parentId,
        String name,
        String path,
        String icon,
        String permissionCode,
        Integer sortOrder,
        Boolean visible,
        Integer status,
        List<MenuItem> children
) {
}
