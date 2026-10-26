package com.example.ai.llm.client;

import com.example.ai.common.api.dto.AppendMessageRequest;
import com.example.ai.common.api.dto.ConversationMessageDto;
import com.example.ai.common.api.dto.CreateSessionResponse;
import com.example.ai.llm.config.LlmProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 会话服务 HTTP 客户端。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@Component
public class ConversationServiceClient {

    private final RestClient restClient;

    public ConversationServiceClient(LlmProperties properties, ClientHttpRequestFactory requestFactory) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getServices().getConversationBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * @param authorization Bearer token
     * @return 新会话
     */
    public CreateSessionResponse createSession(String authorization) {
        return restClient.post()
                .uri("/api/conversation/sessions")
                .header("Authorization", authorization)
                .retrieve()
                .body(CreateSessionResponse.class);
    }

    /**
     * @param sessionId 会话 ID
     * @param limit 条数
     * @param authorization token
     * @return 历史消息
     */
    public List<ConversationMessageDto> listMessages(String sessionId, int limit, String authorization) {
        return restClient.get()
                .uri("/api/conversation/sessions/{sessionId}/messages?limit={limit}", sessionId, limit)
                .header("Authorization", authorization)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ConversationMessageDto>>() {
                });
    }

    /**
     * @param sessionId 会话
     * @param role 角色
     * @param content 内容
     * @param authorization token
     */
    public void appendMessage(String sessionId, String role, String content, String authorization) {
        appendMessage(sessionId, role, content, authorization, null, null);
    }

    /**
     * @param sessionId 会话
     * @param role 角色
     * @param content 内容
     * @param authorization token
     * @param provider 模型提供方
     * @param model 模型名
     */
    public void appendMessage(String sessionId, String role, String content, String authorization,
                              String provider, String model) {
        appendMessage(sessionId, new AppendMessageRequest(role, content, provider, model), authorization);
    }

    /**
     * @param sessionId 会话
     * @param request 消息与模型元数据
     * @param authorization token
     */
    public void appendMessage(String sessionId, AppendMessageRequest request, String authorization) {
        restClient.post()
                .uri("/api/conversation/sessions/{sessionId}/messages", sessionId)
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
