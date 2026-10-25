package com.example.ai.llm.model;

/**
 * 对外暴露的模型提供方信息（不含密钥）。
 *
 * @author 邓鑫鑫
 * @since 0.5.0
 */
public record ChatProviderInfo(String id, String model, boolean defaultProvider) {
}
