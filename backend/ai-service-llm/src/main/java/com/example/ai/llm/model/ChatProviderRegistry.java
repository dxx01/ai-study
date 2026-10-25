package com.example.ai.llm.model;

import com.example.ai.llm.config.LlmProperties;
import com.example.ai.llm.expert.ExpertSystemPrompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 多模型注册表：按配置构建多个 OpenAI 兼容的 ChatClient。
 *
 * <p>DeepSeek、Qwen、Moonshot、OpenAI、Ollama 等都兼容 OpenAI 协议；
 * 原生协议（如 Anthropic）可新增实现并在 {@link #client(String)} 处扩展。</p>
 *
 * @author 邓鑫鑫
 * @since 0.5.0
 */
@Component
public class ChatProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(ChatProviderRegistry.class);

    private final Map<String, ChatClient> clients = new LinkedHashMap<>();
    private final List<ChatProviderInfo> providers = new ArrayList<>();
    private final String defaultProvider;

    public ChatProviderRegistry(LlmProperties properties, ClientHttpRequestFactory requestFactory) {
        LlmProperties.Ai ai = properties.getAi();
        this.defaultProvider = ai.getDefaultProvider();
        String systemPrompt = ExpertSystemPrompt.build(properties.getExpert());

        if (ai.getProviders() == null || ai.getProviders().isEmpty()) {
            throw new IllegalStateException("未配置任何模型提供方，请检查 app.ai.providers");
        }

        for (Map.Entry<String, LlmProperties.Ai.Provider> entry : ai.getProviders().entrySet()) {
            String id = entry.getKey();
            LlmProperties.Ai.Provider provider = entry.getValue();
            validate(id, provider);

            OpenAiApi api = OpenAiApi.builder()
                    .baseUrl(provider.getBaseUrl())
                    .apiKey(provider.getApiKey())
                    .restClientBuilder(RestClient.builder().requestFactory(requestFactory))
                    .build();
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(provider.getModel())
                    .temperature(provider.getTemperature())
                    .maxTokens(provider.getMaxTokens())
                    .build();
            OpenAiChatModel model = OpenAiChatModel.builder()
                    .openAiApi(api)
                    .defaultOptions(options)
                    .build();
            ChatClient client = ChatClient.builder(model).defaultSystem(systemPrompt).build();

            clients.put(id, client);
            providers.add(new ChatProviderInfo(id, provider.getModel(), id.equals(defaultProvider)));
        }

        if (!clients.containsKey(defaultProvider)) {
            throw new IllegalStateException("默认模型提供方 [" + defaultProvider + "] 未在 app.ai.providers 中配置");
        }
        log.info("已加载模型提供方: {}，默认: {}", clients.keySet(), defaultProvider);
    }

    /**
     * @param id 提供方 ID
     * @return ChatClient
     */
    public ChatClient client(String id) {
        ChatClient client = clients.get(id);
        if (client == null) {
            throw new IllegalArgumentException("未知的模型提供方: " + id);
        }
        return client;
    }

    public boolean contains(String id) {
        return clients.containsKey(id);
    }

    public String defaultProvider() {
        return defaultProvider;
    }

    /**
     * @return 提供方列表（不含密钥）
     */
    public List<ChatProviderInfo> list() {
        return List.copyOf(providers);
    }

    private static void validate(String id, LlmProperties.Ai.Provider provider) {
        if (isBlank(id) || provider == null
                || isBlank(provider.getBaseUrl())
                || isBlank(provider.getModel())
                || isBlank(provider.getApiKey())
                || provider.getApiKey().contains("${")) {
            throw new IllegalStateException(
                    "模型提供方 [" + id + "] 配置不完整：需要 base-url / api-key / model，且 api-key 不能是占位符");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
