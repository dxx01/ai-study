package com.example.ai.llm.config;

import com.example.ai.llm.expert.ExpertSystemPrompt;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * ChatClient 装配。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmAiConfig {

    private final Environment environment;

    /**
     * @param environment Spring 环境，用于读取解析后的模型密钥
     */
    public LlmAiConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * 启动时校验模型密钥，避免把未解析的 ${OPENAI_API_KEY} 占位符当成 key 发给模型。
     */
    @PostConstruct
    void validateModelApiKey() {
        String apiKey;
        try {
            apiKey = environment.getProperty("spring.ai.openai.api-key");
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY 未配置或未被解析。请在 backend/ai-service-llm/.env.dev.properties 设置，"
                            + "或配置同名环境变量后重启。",
                    ex);
        }
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("${")) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY 未配置或未被解析。请在 backend/ai-service-llm/.env.dev.properties 设置，"
                            + "或配置同名环境变量后重启。");
        }
    }

    @Bean
    ChatClient expertChatClient(ChatModel chatModel, LlmProperties properties) {
        String systemPrompt = ExpertSystemPrompt.build(properties.getExpert());
        return ChatClient.builder(chatModel).defaultSystem(systemPrompt).build();
    }
}
