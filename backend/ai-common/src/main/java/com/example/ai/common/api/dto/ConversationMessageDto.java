package com.example.ai.common.api.dto;

import java.time.LocalDateTime;

/**
 * 会话消息视图。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
public record ConversationMessageDto(
        String role,
        String content,
        LocalDateTime createdAt
) {
}
