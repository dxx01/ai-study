package com.example.ai.knowledge.config;

import com.example.ai.knowledge.service.KnowledgeIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时从磁盘恢复知识库索引。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@Component
public class KnowledgeStartupLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeStartupLoader.class);

    private final KnowledgeIngestionService knowledgeIngestionService;

    public KnowledgeStartupLoader(KnowledgeIngestionService knowledgeIngestionService) {
        this.knowledgeIngestionService = knowledgeIngestionService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        knowledgeIngestionService.reloadFromDisk();
        log.info("知识库启动扫描完成，已索引文档数: {}", knowledgeIngestionService.listDocuments().size());
    }
}
