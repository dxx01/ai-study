package com.example.ai.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai.knowledge.domain.KnowledgeDocumentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识库文档 Mapper。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocumentEntity> {

    /**
     * @param hash 内容 SHA-256
     * @return 已存在文档
     */
    @Select("""
            SELECT * FROM knowledge_document
            WHERE content_hash = #{hash} AND app_id = #{appId} AND deleted_at IS NULL
            LIMIT 1
            """)
    KnowledgeDocumentEntity selectByHashAndApp(@Param("hash") String hash, @Param("appId") String appId);

    /**
     * @param path 存储路径
     * @return 已存在文档
     */
    @Select("""
            SELECT * FROM knowledge_document
            WHERE storage_path = #{path} AND deleted_at IS NULL
            LIMIT 1
            """)
    KnowledgeDocumentEntity selectByStoragePath(@Param("path") String path);

    /**
     * @return 未删除文档，按创建时间倒序
     */
    @Select("""
            SELECT * FROM knowledge_document
            WHERE deleted_at IS NULL
            ORDER BY created_at DESC
            """)
    List<KnowledgeDocumentEntity> selectActive();

    /**
     * @param appId 应用标识
     * @return 该应用下未删除文档
     */
    @Select("""
            SELECT * FROM knowledge_document
            WHERE app_id = #{appId} AND deleted_at IS NULL
            ORDER BY created_at DESC
            """)
    List<KnowledgeDocumentEntity> selectActiveByApp(@Param("appId") String appId);
}
