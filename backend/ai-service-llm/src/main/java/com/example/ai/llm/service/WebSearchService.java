package com.example.ai.llm.service;

import com.example.ai.common.api.dto.SourceCitation;
import com.example.ai.llm.config.LlmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tavily 联网检索。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@Service
public class WebSearchService {

    private static final Logger log = LoggerFactory.getLogger(WebSearchService.class);

    private final LlmProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public WebSearchService(LlmProperties properties,
                            ObjectMapper objectMapper,
                            ClientHttpRequestFactory clientHttpRequestFactory) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.tavily.com")
                .requestFactory(clientHttpRequestFactory)
                .build();
    }

    public boolean isAvailable() {
        return properties.getWebSearch().isConfigured();
    }

    public List<SourceCitation> search(String query) {
        if (!isAvailable()) {
            return List.of();
        }
        LlmProperties.WebSearch cfg = properties.getWebSearch();
        Map<String, Object> body = Map.of(
                "api_key", cfg.getTavilyApiKey(),
                "query", query,
                "max_results", cfg.getMaxResults(),
                "search_depth", "basic"
        );
        String responseBody = restClient.post()
                .uri("/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        return parseResults(responseBody);
    }

    public String formatForPrompt(List<SourceCitation> webSources) {
        if (webSources.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("【联网检索摘要（供参考，请交叉验证）】\n");
        int i = 1;
        for (SourceCitation source : webSources) {
            sb.append(i++).append(". ").append(source.title()).append("\n");
            sb.append(source.excerpt()).append("\n\n");
        }
        return sb.toString();
    }

    private List<SourceCitation> parseResults(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.path("results");
            List<SourceCitation> citations = new ArrayList<>();
            if (!results.isArray()) {
                return citations;
            }
            for (JsonNode node : results) {
                String title = node.path("title").asText("未命名来源");
                String content = node.path("content").asText("");
                String url = node.path("url").asText("");
                citations.add(SourceCitation.web(title, truncate(content, 500), url));
            }
            return citations;
        } catch (Exception ex) {
            log.warn("解析 Tavily 响应失败", ex);
            return List.of();
        }
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
