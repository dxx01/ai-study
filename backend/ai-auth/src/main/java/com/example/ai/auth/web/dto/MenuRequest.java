package com.example.ai.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 菜单新增/编辑。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
public record MenuRequest(
        Long parentId,
        @NotBlank @Size(max = 64) String name,
        @NotBlank @Size(max = 128) String path,
        @Size(max = 64) String icon,
        @Size(max = 128) String permissionCode,
        Integer sortOrder,
        Boolean visible,
        @jakarta.validation.constraints.Min(0)
        @jakarta.validation.constraints.Max(1)
        Integer status
) {
}
