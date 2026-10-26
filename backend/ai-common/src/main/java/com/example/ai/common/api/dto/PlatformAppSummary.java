package com.example.ai.common.api.dto;

/**
 * 平台 AI 应用摘要（列表/详情展示）。
 *
 * @author 邓鑫鑫
 * @since 0.8.0
 */
public record PlatformAppSummary(
        String code,
        String name,
        String tagline,
        String description,
        String expertId
) {
}
