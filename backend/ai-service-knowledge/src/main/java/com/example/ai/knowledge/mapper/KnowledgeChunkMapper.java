package com.example.ai.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai.knowledge.domain.KnowledgeChunkEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 知识库分块 Mapper。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunkEntity> {

    /**
     * @param documentId 文档 ID
     * @return 按 chunk_index 升序的分块
     */
    @Select("""
            SELECT * FROM knowledge_chunk
            WHERE document_id = #{documentId}
            ORDER BY chunk_index
            """)
    List<KnowledgeChunkEntity> selectByDocument(@Param("documentId") String documentId);

    /**
     * @param id 分块主键
     * @param vectorId 向量库 ID
     */
    @Update("UPDATE knowledge_chunk SET vector_id = #{vectorId} WHERE id = #{id}")
    void updateVectorId(@Param("id") long id, @Param("vectorId") String vectorId);
}
