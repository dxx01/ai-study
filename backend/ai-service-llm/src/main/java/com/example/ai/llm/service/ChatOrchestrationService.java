package com.example.ai.llm.service;

import cn.dev33.satoken.stp.StpUtil;
import com.example.ai.common.api.ChatMode;
import com.example.ai.common.api.dto.ChatRequest;
import com.example.ai.common.api.dto.ChatResponse;
import com.example.ai.common.api.dto.ConversationMessageDto;
import com.example.ai.common.api.dto.KnowledgeSearchHit;
import com.example.ai.common.api.dto.SourceCitation;
import com.example.ai.llm.client.ConversationServiceClient;
import com.example.ai.llm.client.KnowledgeServiceClient;
import com.example.ai.llm.config.LlmProperties;
import com.example.ai.llm.model.ChatProviderRegistry;
import com.example.ai.llm.ratelimit.ChatRateLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 对话编排：会话持久化、RAG、联网与模型调用。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@Service
public class ChatOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(ChatOrchestrationService.class);

    private static final Pattern WEB_INTENT = Pattern.compile(
            "最新|今天|新闻|联网|网上|搜索|近期|今年|202[0-9]|政策|发布");

    private static final int HISTORY_LIMIT = 20;

    private final LlmProperties properties;
    private final KnowledgeServiceClient knowledgeServiceClient;
    private final ConversationServiceClient conversationServiceClient;
    private final WebSearchService webSearchService;
    private final ChatRateLimitService rateLimitService;
    private final ChatProviderRegistry providerRegistry;

    public ChatOrchestrationService(LlmProperties properties,
                                    KnowledgeServiceClient knowledgeServiceClient,
                                    ConversationServiceClient conversationServiceClient,
                                    WebSearchService webSearchService,
                                    ChatRateLimitService rateLimitService,
                                    ChatProviderRegistry providerRegistry) {
        this.properties = properties;
        this.knowledgeServiceClient = knowledgeServiceClient;
        this.conversationServiceClient = conversationServiceClient;
        this.webSearchService = webSearchService;
        this.rateLimitService = rateLimitService;
        this.providerRegistry = providerRegistry;
    }

    /**
     * @param request 对话请求
     * @return 回答与引用
     */
    public ChatResponse chat(ChatRequest request) {
        rateLimitService.check(StpUtil.getLoginIdAsLong());
        String provider = resolveProvider(request.provider());
        ChatClient chatClient = providerRegistry.client(provider);
        String authorization = authorizationHeader();
        String sessionId = resolveSessionId(request.sessionId(), authorization);

        List<KnowledgeSearchHit> hits = knowledgeServiceClient.search(request.message(), authorization);
        List<SourceCitation> sources = new ArrayList<>(toLocalCitations(hits));

        ChatMode modeUsed = resolveMode(request.mode(), request.message());
        boolean webUsed = false;
        String userContent = buildUserContentWithLocalRag(request.message(), hits, loadHistory(sessionId, authorization));

        if (modeUsed == ChatMode.WEB || modeUsed == ChatMode.AUTO) {
            if (webSearchService.isAvailable()) {
                List<SourceCitation> webSources = webSearchService.search(request.message());
                if (!webSources.isEmpty()) {
                    webUsed = true;
                    sources.addAll(webSources);
                    userContent = webSearchService.formatForPrompt(webSources) + "\n" + userContent;
                } else if (modeUsed == ChatMode.WEB) {
                    userContent = "【说明】联网检索未返回结果，请结合本地知识库与常识作答，并提示用户稍后重试。\n\n" + userContent;
                }
            } else if (modeUsed == ChatMode.WEB) {
                userContent = "【说明】未配置联网检索（TAVILY_API_KEY），请仅依据本地知识库与常识回答，并提示管理员配置。\n\n"
                        + userContent;
            }
        }

        String answer = chatClient.prompt().user(userContent).call().content();

        conversationServiceClient.appendMessage(sessionId, "user", request.message(), authorization);
        conversationServiceClient.appendMessage(sessionId, "assistant", answer, authorization);

        LlmProperties.Expert expert = properties.getExpert();
        return new ChatResponse(
                sessionId,
                expert.getId(),
                expert.getDisplayName(),
                request.message(),
                answer,
                modeUsed,
                webUsed,
                sources,
                expert.getDisclaimer()
        );
    }

    private String resolveSessionId(String requested, String authorization) {
        if (requested != null && !requested.isBlank()) {
            try {
                conversationServiceClient.listMessages(requested, 1, authorization);
                return requested;
            } catch (RestClientResponseException ex) {
                log.debug("会话无效或无权访问，将创建新会话: {}", requested);
            }
        }
        return conversationServiceClient.createSession(authorization).sessionId();
    }

    private List<ConversationMessageDto> loadHistory(String sessionId, String authorization) {
        try {
            return conversationServiceClient.listMessages(sessionId, HISTORY_LIMIT, authorization);
        } catch (Exception ex) {
            log.warn("加载会话历史失败: sessionId={}", sessionId, ex);
            return List.of();
        }
    }

    private static String authorizationHeader() {
        return "Bearer " + StpUtil.getTokenValue();
    }

    private String resolveProvider(String requested) {
        if (requested == null || requested.isBlank()) {
            return providerRegistry.defaultProvider();
        }
        if (!providerRegistry.contains(requested)) {
            throw new IllegalArgumentException("未知的模型提供方: " + requested);
        }
        return requested;
    }

    private ChatMode resolveMode(ChatMode requested, String message) {        if (requested != ChatMode.AUTO) {
            return requested;
        }
        if (webSearchService.isAvailable() && WEB_INTENT.matcher(message).find()) {
            return ChatMode.WEB;
        }
        return ChatMode.LOCAL;
    }

    private static List<SourceCitation> toLocalCitations(List<KnowledgeSearchHit> hits) {
        List<SourceCitation> citations = new ArrayList<>();
        for (KnowledgeSearchHit hit : hits) {
            citations.add(SourceCitation.local(
                    hit.documentId(),
                    hit.fileName(),
                    truncate(hit.text(), 300),
                    hit.score()));
        }
        return citations;
    }

    private static String buildUserContentWithLocalRag(String question,
                                                       List<KnowledgeSearchHit> hits,
                                                       List<ConversationMessageDto> history) {
        StringBuilder sb = new StringBuilder();
        if (!history.isEmpty()) {
            sb.append("【近期对话历史】\n");
            for (ConversationMessageDto msg : history) {
                sb.append(msg.role()).append(": ").append(truncate(msg.content(), 500)).append("\n");
            }
            sb.append("\n");
        }
        if (!hits.isEmpty()) {
            sb.append("【本地知识库片段（回答须优先依据以下内容）】\n");
            int index = 1;
            for (KnowledgeSearchHit hit : hits) {
                sb.append(index++).append(". ").append(hit.fileName()).append("\n");
                sb.append(truncate(hit.text(), 800)).append("\n\n");
            }
        }
        sb.append("【用户问题】\n").append(question);
        return sb.toString();
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
