package com.example.ai.knowledge.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量库与文档分块装配。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@Configuration
@EnableConfigurationProperties(KnowledgeProperties.class)
public class KnowledgeAiConfig {

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    TokenTextSplitter knowledgeTextSplitter() {
        return new TokenTextSplitter(800, 100, 10, 5000, true);
    }
}
