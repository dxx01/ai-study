package com.example.ai.knowledge.service;

import com.example.ai.common.api.dto.DocumentSummary;
import com.example.ai.common.api.dto.KnowledgeSearchHit;
import com.example.ai.knowledge.config.KnowledgeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 知识库上传、索引与向量检索。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@Service
public class KnowledgeIngestionService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestionService.class);

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "pdf", "doc", "docx", "txt", "md", "html", "htm", "ppt", "pptx"
    );

    public static final String META_DOCUMENT_ID = "documentId";
    public static final String META_FILE_NAME = "fileName";
    public static final String META_UPLOADED_AT = "uploadedAt";

    private final KnowledgeProperties properties;
    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;
    private final Map<String, DocumentSummary> index = new ConcurrentHashMap<>();

    public KnowledgeIngestionService(KnowledgeProperties properties,
                                       VectorStore vectorStore,
                                       TokenTextSplitter textSplitter) {
        this.properties = properties;
        this.vectorStore = vectorStore;
        this.textSplitter = textSplitter;
    }

    /**
     * @param file 上传文件
     * @return 文档摘要
     */
    public DocumentSummary ingest(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }
        validateExtension(file.getOriginalFilename());
        Path storageDir = Path.of(properties.getStorageDir()).toAbsolutePath().normalize();
        Files.createDirectories(storageDir);

        String documentId = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
        Path saved = storageDir.resolve(documentId + "_" + sanitizeFileName(originalName));
        file.transferTo(saved);

        List<Document> documents = new TikaDocumentReader(new FileSystemResource(saved)).get();
        if (documents.isEmpty()) {
            throw new IllegalArgumentException("无法从文件中提取文本，请换 PDF/Word/TXT 等格式");
        }

        Instant uploadedAt = Instant.now();
        for (Document doc : documents) {
            doc.getMetadata().put(META_DOCUMENT_ID, documentId);
            doc.getMetadata().put(META_FILE_NAME, originalName);
            doc.getMetadata().put(META_UPLOADED_AT, uploadedAt.toString());
        }

        List<Document> chunks = textSplitter.apply(documents);
        vectorStore.add(chunks);

        DocumentSummary summary = new DocumentSummary(
                documentId,
                originalName,
                Files.size(saved),
                uploadedAt,
                chunks.size()
        );
        index.put(documentId, summary);
        return summary;
    }

    /**
     * @return 已索引文档列表
     */
    public List<DocumentSummary> listDocuments() {
        return new ArrayList<>(index.values());
    }

    /**
     * 从磁盘目录重新加载未索引文件。
     */
    public void reloadFromDisk() throws IOException {
        Path storageDir = Path.of(properties.getStorageDir()).toAbsolutePath().normalize();
        if (!Files.isDirectory(storageDir)) {
            return;
        }
        try (var paths = Files.list(storageDir)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    ingestExistingFile(path);
                } catch (Exception ex) {
                    log.warn("跳过无法解析的知识库文件: {}", path, ex);
                }
            });
        }
    }

    /**
     * @param query 用户问题
     * @return 检索命中片段
     */
    public List<KnowledgeSearchHit> searchHits(String query) {
        List<Document> documents = searchLocal(query);
        List<KnowledgeSearchHit> hits = new ArrayList<>();
        for (Document doc : documents) {
            String documentId = String.valueOf(doc.getMetadata().getOrDefault(META_DOCUMENT_ID, "unknown"));
            String fileName = String.valueOf(doc.getMetadata().getOrDefault(META_FILE_NAME, "本地资料"));
            hits.add(new KnowledgeSearchHit(documentId, fileName, doc.getText(), doc.getScore()));
        }
        return hits;
    }

    private List<Document> searchLocal(String query) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(properties.getRagTopK())
                .similarityThreshold(properties.getSimilarityThreshold())
                .build());
    }

    private void ingestExistingFile(Path saved) throws IOException {
        String fileName = saved.getFileName().toString();
        int underscore = fileName.indexOf('_');
        if (underscore <= 0) {
            return;
        }
        String documentId = fileName.substring(0, underscore);
        if (index.containsKey(documentId)) {
            return;
        }
        String originalName = fileName.substring(underscore + 1);
        List<Document> documents = new TikaDocumentReader(new FileSystemResource(saved)).get();
        if (documents.isEmpty()) {
            return;
        }
        Instant uploadedAt = Files.getLastModifiedTime(saved).toInstant();
        for (Document doc : documents) {
            doc.getMetadata().put(META_DOCUMENT_ID, documentId);
            doc.getMetadata().put(META_FILE_NAME, originalName);
            doc.getMetadata().put(META_UPLOADED_AT, uploadedAt.toString());
        }
        List<Document> chunks = textSplitter.apply(documents);
        vectorStore.add(chunks);
        index.put(documentId, new DocumentSummary(documentId, originalName, Files.size(saved), uploadedAt, chunks.size()));
    }

    private static void validateExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("文件名无效");
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            throw new IllegalArgumentException("不支持的文件类型，允许: " + ALLOWED_EXTENSIONS);
        }
        String ext = originalFilename.substring(dot + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("不支持的文件类型: ." + ext);
        }
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
