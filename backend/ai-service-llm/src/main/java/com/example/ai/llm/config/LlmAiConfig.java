package com.example.ai.llm.config;

import com.example.ai.llm.expert.ExpertSystemPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 装配。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmAiConfig {

    @Bean
    ChatClient expertChatClient(ChatModel chatModel, LlmProperties properties) {
        String systemPrompt = ExpertSystemPrompt.build(properties.getExpert());
        return ChatClient.builder(chatModel).defaultSystem(systemPrompt).build();
    }
}
