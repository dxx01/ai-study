package com.example.ai.auth.web.dto;

/**
 * 修改用户状态。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
public record UpdateUserStatusRequest(
        @jakarta.validation.constraints.Min(0)
        @jakarta.validation.constraints.Max(1)
        Integer status
) {
}
