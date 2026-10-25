package com.example.ai.knowledge.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.ai.common.api.dto.DocumentSummary;
import com.example.ai.common.api.dto.KnowledgeSearchHit;
import com.example.ai.common.api.dto.KnowledgeSearchRequest;
import com.example.ai.common.security.AiPermissions;
import com.example.ai.knowledge.service.KnowledgeIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 知识库管理 API。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@SaCheckLogin
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestionService knowledgeIngestionService;

    public KnowledgeController(KnowledgeIngestionService knowledgeIngestionService) {
        this.knowledgeIngestionService = knowledgeIngestionService;
    }

    /**
     * 向量检索（供 LLM 服务与用户调试）。
     */
    @SaCheckPermission(AiPermissions.CHAT)
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<KnowledgeSearchHit> search(@Valid @RequestBody KnowledgeSearchRequest request) {
        return knowledgeIngestionService.searchHits(request.query());
    }

    @SaCheckPermission(AiPermissions.KNOWLEDGE_WRITE)
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentSummary upload(@RequestParam("file") MultipartFile file) throws IOException {
        return knowledgeIngestionService.ingest(file);
    }

    @SaCheckPermission(AiPermissions.KNOWLEDGE_READ)
    @GetMapping("/documents")
    public List<DocumentSummary> list() {
        return knowledgeIngestionService.listDocuments();
    }

    @SaCheckPermission(AiPermissions.KNOWLEDGE_ADMIN)
    @PostMapping("/reload")
    public Map<String, Object> reload() throws IOException {
        knowledgeIngestionService.reloadFromDisk();
        return Map.of(
                "reloaded", true,
                "documentCount", knowledgeIngestionService.listDocuments().size()
        );
    }
}
