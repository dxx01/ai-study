package com.example.ai.llm.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.ai.common.api.ChatMode;
import com.example.ai.common.api.dto.ChatRequest;
import com.example.ai.common.api.dto.ChatResponse;
import com.example.ai.common.security.AiPermissions;
import com.example.ai.llm.model.ChatProviderInfo;
import com.example.ai.llm.model.ChatProviderRegistry;
import com.example.ai.llm.service.ChatOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 对话 API。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@SaCheckLogin
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatOrchestrationService chatOrchestrationService;
    private final ChatProviderRegistry chatProviderRegistry;

    public ChatController(ChatOrchestrationService chatOrchestrationService,
                          ChatProviderRegistry chatProviderRegistry) {
        this.chatOrchestrationService = chatOrchestrationService;
        this.chatProviderRegistry = chatProviderRegistry;
    }

    @SaCheckPermission(AiPermissions.CHAT)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatOrchestrationService.chat(request);
    }

    @SaCheckPermission(AiPermissions.CHAT)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chatQuick(@RequestParam(defaultValue = "你好") String message,
                                  @RequestParam(required = false) String sessionId,
                                  @RequestParam(required = false, defaultValue = "AUTO") ChatMode mode,
                                  @RequestParam(required = false) String provider) {
        return chatOrchestrationService.chat(new ChatRequest(message, sessionId, mode, provider));
    }

    /**
     * @return 可用模型提供方（不含密钥）
     */
    @SaCheckPermission(AiPermissions.CHAT)
    @GetMapping(value = "/providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ChatProviderInfo> providers() {
        return chatProviderRegistry.list();
    }
}
