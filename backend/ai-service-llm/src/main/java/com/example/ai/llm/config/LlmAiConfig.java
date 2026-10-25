package com.example.ai.llm.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 配置入口：模型提供方由 {@code ChatProviderRegistry} 按 app.ai 配置装配。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmAiConfig {
}
