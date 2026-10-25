package com.example.ai.conversation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai.conversation.domain.ChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会话 Mapper。
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {

    /**
     * @param sessionId 会话 ID
     * @param ownerUserId 所属用户
     * @return 会话
     */
    @Select("SELECT * FROM chat_session WHERE id = #{sessionId} AND owner_user_id = #{ownerUserId} LIMIT 1")
    ChatSessionEntity selectByIdAndOwner(@Param("sessionId") String sessionId,
                                         @Param("ownerUserId") Long ownerUserId);
}
