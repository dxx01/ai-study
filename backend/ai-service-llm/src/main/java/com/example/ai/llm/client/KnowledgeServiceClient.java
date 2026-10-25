package com.example.ai.llm.client;

import com.example.ai.common.api.dto.KnowledgeSearchHit;
import com.example.ai.common.api.dto.KnowledgeSearchRequest;
import com.example.ai.llm.config.LlmProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 知识库服务 HTTP 客户端。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@Component
public class KnowledgeServiceClient {

    private final RestClient restClient;

    public KnowledgeServiceClient(LlmProperties properties, ClientHttpRequestFactory requestFactory) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getServices().getKnowledgeBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * @param query 检索词
     * @param authorization 请求头 Authorization 完整值
     * @return 命中片段
     */
    public List<KnowledgeSearchHit> search(String query, String authorization) {
        return restClient.post()
                .uri("/api/knowledge/search")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new KnowledgeSearchRequest(query))
                .retrieve()
                .body(new ParameterizedTypeReference<List<KnowledgeSearchHit>>() {
                });
    }
}
