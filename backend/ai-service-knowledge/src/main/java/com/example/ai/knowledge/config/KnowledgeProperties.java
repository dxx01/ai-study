package com.example.ai.knowledge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 知识库服务配置。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@ConfigurationProperties(prefix = "app.knowledge")
public class KnowledgeProperties {

    private String storageDir = "./data/knowledge-base";
    private int ragTopK = 5;
    private double similarityThreshold = 0.65;
    private String embeddingModel = "all-MiniLM-L6-v2";

    public String getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    public int getRagTopK() {
        return ragTopK;
    }

    public void setRagTopK(int ragTopK) {
        this.ragTopK = ragTopK;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }
}
