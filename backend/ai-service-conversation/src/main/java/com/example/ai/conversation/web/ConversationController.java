package com.example.ai.conversation.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.example.ai.common.api.dto.AppendMessageRequest;
import com.example.ai.common.api.dto.ConversationMessageDto;
import com.example.ai.common.api.dto.CreateSessionResponse;
import com.example.ai.common.security.AiPermissions;
import com.example.ai.conversation.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会话 API。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@SaCheckLogin
@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @SaCheckPermission(AiPermissions.CHAT)
    @PostMapping(value = "/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateSessionResponse createSession() {
        return conversationService.createSession(StpUtil.getLoginIdAsLong());
    }

    @SaCheckPermission(AiPermissions.CHAT)
    @GetMapping("/sessions/{sessionId}/messages")
    public List<ConversationMessageDto> listMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "50") int limit) {
        return conversationService.listMessages(sessionId, StpUtil.getLoginIdAsLong(), limit);
    }

    @SaCheckPermission(AiPermissions.CHAT)
    @PostMapping(value = "/sessions/{sessionId}/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void appendMessage(@PathVariable String sessionId, @Valid @RequestBody AppendMessageRequest request) {
        conversationService.appendMessage(sessionId, StpUtil.getLoginIdAsLong(), request);
    }
}
