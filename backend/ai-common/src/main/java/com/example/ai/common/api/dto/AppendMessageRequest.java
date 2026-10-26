package com.example.ai.common.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 追加会话消息（含模型调用元数据）。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
public record AppendMessageRequest(
        @NotBlank
        @Pattern(regexp = "user|assistant", message = "role 必须为 user 或 assistant")
        String role,
        @NotBlank @Size(max = 16000) String content,
        @Size(max = 32) String provider,
        @Size(max = 64) String model,
        @Min(0) Integer promptTokens,
        @Min(0) Integer completionTokens,
        @Min(0) Integer latencyMs,
        @Size(max = 32) String finishReason,
        @Size(max = 64) String traceId
) {
    /**
     * 兼容只传 role/content 的调用方。
     *
     * @param role 角色
     * @param content 内容
     */
    public AppendMessageRequest(String role, String content) {
        this(role, content, null, null, null, null, null, null, null);
    }

    /**
     * 兼容只传模型信息的调用方。
     *
     * @param role 角色
     * @param content 内容
     * @param provider 提供方
     * @param model 模型名
     */
    public AppendMessageRequest(String role, String content, String provider, String model) {
        this(role, content, provider, model, null, null, null, null, null);
    }
}
