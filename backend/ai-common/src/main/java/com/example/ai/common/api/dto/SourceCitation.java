package com.example.ai.common.api.dto;

/**
 * 回答引用来源（本地 RAG 或联网）。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
public record SourceCitation(
        String type,
        String documentId,
        String title,
        String excerpt,
        Double score
) {
    /**
     * @param documentId 文档 ID
     * @param title 标题
     * @param excerpt 摘要片段
     * @param score 相似度
     * @return 本地知识库引用
     */
    public static SourceCitation local(String documentId, String title, String excerpt, Double score) {
        return new SourceCitation("LOCAL_KNOWLEDGE", documentId, title, excerpt, score);
    }

    /**
     * @param title 标题
     * @param excerpt 摘要
     * @param url 链接
     * @return 联网引用
     */
    public static SourceCitation web(String title, String excerpt, String url) {
        return new SourceCitation("WEB_SEARCH", url, title, excerpt, null);
    }
}
