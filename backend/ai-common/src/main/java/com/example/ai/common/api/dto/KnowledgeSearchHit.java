package com.example.ai.common.api.dto;

/**
 * 知识库检索命中片段（供 LLM 服务拼 RAG 上下文）。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
public record KnowledgeSearchHit(
        String documentId,
        String fileName,
        String text,
        Double score
) {
}
