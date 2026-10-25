package com.example.ai.common.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 知识库向量检索请求。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
public record KnowledgeSearchRequest(
        @NotBlank @Size(max = 2000) String query
) {
}
