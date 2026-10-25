package com.example.ai.common.api.dto;

import java.time.Instant;

/**
 * 知识库文档摘要。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
public record DocumentSummary(
        String documentId,
        String fileName,
        long sizeBytes,
        Instant uploadedAt,
        int chunkCount
) {
}
