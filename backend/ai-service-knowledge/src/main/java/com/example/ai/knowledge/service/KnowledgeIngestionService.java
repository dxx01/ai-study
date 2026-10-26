package com.example.ai.knowledge.service;

import com.example.ai.common.api.dto.DocumentSummary;
import com.example.ai.common.api.dto.KnowledgeSearchHit;
import com.example.ai.knowledge.config.KnowledgeProperties;
import com.example.ai.knowledge.domain.KnowledgeChunkEntity;
import com.example.ai.knowledge.domain.KnowledgeDocumentEntity;
import com.example.ai.knowledge.mapper.KnowledgeChunkMapper;
import com.example.ai.knowledge.mapper.KnowledgeDocumentMapper;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 知识库上传、索引、持久化与向量检索。
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

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    public static final String META_DOCUMENT_ID = "documentId";
    public static final String META_FILE_NAME = "fileName";
    public static final String META_UPLOADED_AT = "uploadedAt";
    public static final String META_APP_ID = "appId";

    private final KnowledgeProperties properties;
    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;
    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;
    private final PlatformAppValidator platformAppValidator;

    public KnowledgeIngestionService(KnowledgeProperties properties,
                                     VectorStore vectorStore,
                                     TokenTextSplitter textSplitter,
                                     KnowledgeDocumentMapper documentMapper,
                                     KnowledgeChunkMapper chunkMapper,
                                     PlatformAppValidator platformAppValidator) {
        this.properties = properties;
        this.vectorStore = vectorStore;
        this.textSplitter = textSplitter;
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.platformAppValidator = platformAppValidator;
    }

    /**
     * @param file 上传文件
     * @param ownerUserId 上传用户
     * @return 文档摘要
     */
    public DocumentSummary ingest(MultipartFile file, long ownerUserId, String appId, int category)
            throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }
        String resolvedAppId = platformAppValidator.requireEnabled(appId);
        validateExtension(file.getOriginalFilename());
        Path storageDir = storageDir(resolvedAppId);
        Files.createDirectories(storageDir);

        String documentId = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
        Path saved = storageDir.resolve(documentId + "_" + sanitizeFileName(originalName));
        file.transferTo(saved);
        return indexFile(saved, originalName, ownerUserId, documentId, resolvedAppId, category);
    }

    /**
     * @param appId 应用标识
     * @return 已索引文档列表
     */
    public List<DocumentSummary> listDocuments(String appId) {
        String resolvedAppId = platformAppValidator.requireEnabled(appId);
        return documentMapper.selectActiveByApp(resolvedAppId).stream().map(this::toSummary).toList();
    }

    /**
     * @return 全部应用下未删除文档数量
     */
    public int countAllDocuments() {
        return documentMapper.selectActive().size();
    }

    /**
     * 启动时恢复：先从 MySQL 分块重建向量索引，再扫描未入库的磁盘文件。
     */
    public void reloadFromDisk() throws IOException {
        restoreFromDatabase();

        Path rootDir = Path.of(properties.getStorageDir()).toAbsolutePath().normalize();
        if (!Files.isDirectory(rootDir)) {
            return;
        }
        scanDiskDirectory(rootDir, PlatformAppValidator.DEFAULT_APP_ID);
        try (var dirs = Files.list(rootDir)) {
            dirs.filter(Files::isDirectory).forEach(dir -> {
                String appId = dir.getFileName().toString();
                scanDiskDirectory(dir, appId);
            });
        }
    }

    private void scanDiskDirectory(Path storageDir, String appId) {
        if (!Files.isDirectory(storageDir)) {
            return;
        }
        try (var paths = Files.list(storageDir)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    String storagePath = path.toAbsolutePath().normalize().toString();
                    if (documentMapper.selectByStoragePath(storagePath) != null) {
                        return;
                    }
                    String fileName = path.getFileName().toString();
                    String documentId = extractDocumentId(fileName);
                    String originalName = extractOriginalName(fileName);
                    indexFile(path, originalName, 0L, documentId, appId, 0);
                } catch (Exception ex) {
                    log.warn("跳过无法解析的知识库文件: {}", path, ex);
                }
            });
        } catch (IOException ex) {
            log.warn("扫描知识库目录失败: {}", storageDir, ex);
        }
    }

    /**
     * @param query 用户问题
     * @return 检索命中片段
     */
    public List<KnowledgeSearchHit> searchHits(String query, String appId) {
        String resolvedAppId = platformAppValidator.requireEnabled(appId);
        Set<String> allowedDocIds = documentMapper.selectActiveByApp(resolvedAppId).stream()
                .map(KnowledgeDocumentEntity::getId)
                .collect(Collectors.toSet());
        List<Document> documents = searchLocal(query);
        List<KnowledgeSearchHit> hits = new ArrayList<>();
        for (Document doc : documents) {
            String documentId = String.valueOf(doc.getMetadata().getOrDefault(META_DOCUMENT_ID, "unknown"));
            if (!allowedDocIds.contains(documentId)) {
                continue;
            }
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

    private DocumentSummary indexFile(Path saved, String originalName, long ownerUserId, String documentId,
                                      String appId, int category)
            throws IOException {
        String storagePath = saved.toAbsolutePath().normalize().toString();
        String hash = sha256(saved);
        KnowledgeDocumentEntity existing = documentMapper.selectByHashAndApp(hash, appId);
        if (existing != null) {
            if (!existing.getStoragePath().equals(storagePath)) {
                Files.deleteIfExists(saved);
            }
            return toSummary(existing);
        }

        KnowledgeDocumentEntity entity = new KnowledgeDocumentEntity();
        entity.setId(documentId);
        entity.setOwnerUserId(ownerUserId);
        entity.setAppId(appId);
        entity.setCategory(category);
        entity.setFileName(originalName);
        entity.setStoragePath(storagePath);
        entity.setContentHash(hash);
        entity.setSizeBytes(Files.size(saved));
        entity.setChunkCount(0);
        entity.setEmbeddingModel(properties.getEmbeddingModel());
        entity.setStatus("UPLOADING");
        documentMapper.insert(entity);

        try {
            List<Document> documents = new TikaDocumentReader(new FileSystemResource(saved)).get();
            if (documents.isEmpty()) {
                throw new IllegalArgumentException("无法从文件中提取文本，请换 PDF/Word/TXT 等格式");
            }
            Instant uploadedAt = entity.getCreatedAt() == null
                    ? Instant.now()
                    : entity.getCreatedAt().atZone(SHANGHAI).toInstant();
            for (Document doc : documents) {
                doc.getMetadata().put(META_DOCUMENT_ID, documentId);
                doc.getMetadata().put(META_FILE_NAME, originalName);
                doc.getMetadata().put(META_UPLOADED_AT, uploadedAt.toString());
                doc.getMetadata().put(META_APP_ID, appId);
            }
            List<Document> chunks = textSplitter.apply(documents);
            if (chunks.isEmpty()) {
                throw new IllegalArgumentException("文件分块结果为空");
            }
            vectorStore.add(chunks);

            for (int i = 0; i < chunks.size(); i++) {
                Document chunk = chunks.get(i);
                KnowledgeChunkEntity row = new KnowledgeChunkEntity();
                row.setDocumentId(documentId);
                row.setChunkIndex(i);
                row.setContent(chunk.getText());
                row.setVectorId(chunk.getId());
                chunkMapper.insert(row);
            }

            KnowledgeDocumentEntity done = new KnowledgeDocumentEntity();
            done.setId(documentId);
            done.setStatus("INDEXED");
            done.setChunkCount(chunks.size());
            documentMapper.updateById(done);
            log.info("知识库文档索引完成: id={} name={} chunks={}", documentId, originalName, chunks.size());
            return toSummary(documentMapper.selectById(documentId));
        } catch (RuntimeException ex) {
            KnowledgeDocumentEntity failed = new KnowledgeDocumentEntity();
            failed.setId(documentId);
            failed.setStatus("FAILED");
            failed.setErrorMessage(truncate(ex.getMessage(), 500));
            documentMapper.updateById(failed);
            throw ex;
        }
    }

    private void restoreFromDatabase() {
        for (KnowledgeDocumentEntity document : documentMapper.selectActive()) {
            List<KnowledgeChunkEntity> chunks = chunkMapper.selectByDocument(document.getId());
            if (chunks.isEmpty()) {
                continue;
            }
            List<Document> documents = new ArrayList<>();
            for (KnowledgeChunkEntity chunk : chunks) {
                String docAppId = document.getAppId() != null
                        ? document.getAppId()
                        : PlatformAppValidator.DEFAULT_APP_ID;
                documents.add(new Document(chunk.getContent(), Map.of(
                        META_DOCUMENT_ID, document.getId(),
                        META_FILE_NAME, document.getFileName(),
                        META_APP_ID, docAppId
                )));
            }
            vectorStore.add(documents);
            for (int i = 0; i < chunks.size(); i++) {
                chunkMapper.updateVectorId(chunks.get(i).getId(), documents.get(i).getId());
            }
            log.info("从数据库恢复知识库文档: id={} chunks={}", document.getId(), chunks.size());
        }
    }

    private DocumentSummary toSummary(KnowledgeDocumentEntity entity) {
        LocalDateTime createdAt = entity.getCreatedAt();
        Instant uploadedAt = createdAt == null ? Instant.now() : createdAt.atZone(SHANGHAI).toInstant();
        long size = entity.getSizeBytes() == null ? 0 : entity.getSizeBytes();
        int chunkCount = entity.getChunkCount() == null ? 0 : entity.getChunkCount();
        String app = entity.getAppId() != null ? entity.getAppId() : PlatformAppValidator.DEFAULT_APP_ID;
        int cat = entity.getCategory() == null ? 0 : entity.getCategory();
        return new DocumentSummary(entity.getId(), app, cat, entity.getFileName(), size, uploadedAt, chunkCount);
    }

    private Path storageDir(String appId) {
        return Path.of(properties.getStorageDir(), appId).toAbsolutePath().normalize();
    }

    private static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(Files.readAllBytes(path));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 不可用", ex);
        }
    }

    private static String extractDocumentId(String fileName) {
        int underscore = fileName.indexOf('_');
        if (underscore > 0) {
            String candidate = fileName.substring(0, underscore);
            try {
                return UUID.fromString(candidate).toString();
            } catch (IllegalArgumentException ignored) {
                // 非 UUID 前缀，回退生成
            }
        }
        return UUID.randomUUID().toString();
    }

    private static String extractOriginalName(String fileName) {
        int underscore = fileName.indexOf('_');
        return underscore > 0 ? fileName.substring(underscore + 1) : fileName;
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "索引失败";
        }
        return text.length() <= max ? text : text.substring(0, max);
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
