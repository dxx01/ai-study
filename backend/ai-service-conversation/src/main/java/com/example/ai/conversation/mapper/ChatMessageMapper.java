package com.example.ai.conversation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai.conversation.domain.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息 Mapper。
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {

    /**
     * 按时间倒序取最近 N 条（业务层再反转为正序）。
     *
     * @param sessionId 会话
     * @param limit 条数
     * @return 消息列表
     */
    @Select("""
            SELECT * FROM chat_message
            WHERE session_id = #{sessionId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<ChatMessageEntity> selectRecentBySession(@Param("sessionId") String sessionId,
                                                  @Param("limit") int limit);
}
