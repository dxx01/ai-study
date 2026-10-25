package com.example.ai.common.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 追加会话消息。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
public record AppendMessageRequest(
        @NotBlank
        @Pattern(regexp = "user|assistant", message = "role 必须为 user 或 assistant")
        String role,
        @NotBlank @Size(max = 16000) String content
) {
}
