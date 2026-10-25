package com.example.ai.common.api.dto;

import com.example.ai.common.api.ChatMode;

import java.util.List;

/**
 * 对话响应。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
public record ChatResponse(
        String sessionId,
        String expertId,
        String expertName,
        String question,
        String answer,
        ChatMode modeUsed,
        boolean webSearchUsed,
        List<SourceCitation> sources,
        String disclaimer
) {
}
