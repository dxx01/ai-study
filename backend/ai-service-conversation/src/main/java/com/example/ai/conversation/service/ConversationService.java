package com.example.ai.conversation.service;

import com.example.ai.common.api.dto.AppendMessageRequest;
import com.example.ai.common.api.dto.ConversationMessageDto;
import com.example.ai.common.api.dto.CreateSessionResponse;
import com.example.ai.common.mybatis.DbTimeZones;
import com.example.ai.conversation.domain.ChatMessageEntity;
import com.example.ai.conversation.domain.ChatSessionEntity;
import com.example.ai.conversation.mapper.ChatMessageMapper;
import com.example.ai.conversation.mapper.ChatSessionMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 会话与消息业务。
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
@Service
public class ConversationService {

    private static final int MAX_LIMIT = 100;

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    public ConversationService(ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
    }

    /**
     * @param ownerUserId 当前用户主键（须与 Sa-Token loginId 一致；会话 owner 由自动填充兜底）
     * @return 新会话 ID
     */
    @Transactional
    public CreateSessionResponse createSession(Long ownerUserId) {
        ChatSessionEntity session = new ChatSessionEntity();
        session.setId(UUID.randomUUID().toString());
        session.setOwnerUserId(ownerUserId);
        session.setTitle("新对话");
        session.setMessageCount(0);
        sessionMapper.insert(session);
        return new CreateSessionResponse(session.getId());
    }

    /**
     * @param sessionId 会话 ID
     * @param ownerUserId 所属用户
     * @param limit 条数上限
     * @return 时间正序消息（稳定排序：created_at, id）
     */
    @Transactional(readOnly = true)
    public List<ConversationMessageDto> listMessages(String sessionId, Long ownerUserId, int limit) {
        requireSession(sessionId, ownerUserId);
        int size = Math.min(Math.max(limit, 1), MAX_LIMIT);
        List<ChatMessageEntity> recent = messageMapper.selectRecentBySession(sessionId, size);
        List<ConversationMessageDto> result = new ArrayList<>();
        for (int i = recent.size() - 1; i >= 0; i--) {
            result.add(toDto(recent.get(i)));
        }
        return result;
    }

    /**
     * @param sessionId 会话
     * @param ownerUserId 用户
     * @param request 消息体
     */
    @Transactional
    public void appendMessage(String sessionId, Long ownerUserId, AppendMessageRequest request) {
        ChatSessionEntity session = requireSession(sessionId, ownerUserId);
        ChatMessageEntity message = new ChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole(request.role());
        message.setContent(request.content());
        message.setProvider(request.provider());
        message.setModel(request.model());
        message.setPromptTokens(request.promptTokens());
        message.setCompletionTokens(request.completionTokens());
        message.setLatencyMs(request.latencyMs());
        message.setFinishReason(request.finishReason());
        message.setTraceId(request.traceId());
        messageMapper.insert(message);

        LocalDateTime now = LocalDateTime.now(DbTimeZones.SHANGHAI);
        var update = Wrappers.<ChatSessionEntity>lambdaUpdate()
                .eq(ChatSessionEntity::getId, sessionId)
                .setSql("message_count = message_count + 1")
                .set(ChatSessionEntity::getLastMessageAt, now)
                .set(ChatSessionEntity::getUpdatedAt, now);
        if ("user".equals(request.role()) && isDefaultTitle(session.getTitle())) {
            update.set(ChatSessionEntity::getTitle, titleFrom(request.content()));
        }
        sessionMapper.update(null, update);
    }

    private static boolean isDefaultTitle(String title) {
        return title == null || title.isBlank() || "新对话".equals(title);
    }

    private static String titleFrom(String content) {
        String text = content == null ? "" : content.strip().replaceAll("\\s+", " ");
        if (text.isEmpty()) {
            return "新对话";
        }
        return text.length() <= 30 ? text : text.substring(0, 30) + "…";
    }

    private ChatSessionEntity requireSession(String sessionId, Long ownerUserId) {
        ChatSessionEntity session = sessionMapper.selectByIdAndOwner(sessionId, ownerUserId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在或无权访问");
        }
        return session;
    }

    private static ConversationMessageDto toDto(ChatMessageEntity entity) {
        return new ConversationMessageDto(entity.getRole(), entity.getContent(), entity.getCreatedAt());
    }
}
