package com.example.ai.common.api.dto;

import com.example.ai.common.api.ChatMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 对话请求（LLM 服务 API）。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
public record ChatRequest(
        @NotBlank(message = "message 不能为空")
        @Size(max = 4000, message = "message 过长")
        String message,
        @Size(max = 64, message = "sessionId 过长")
        String sessionId,
        ChatMode mode,
        @Size(max = 64, message = "provider 过长")
        String provider
) {
    public ChatRequest {
        if (mode == null) {
            mode = ChatMode.AUTO;
        }
    }
}
