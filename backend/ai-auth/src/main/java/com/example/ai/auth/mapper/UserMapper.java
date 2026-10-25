package com.example.ai.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.ai.auth.domain.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper。
 *
 * <p>注意：自定义 {@code @Select} 不会走 {@code autoResultMap}，JSON 列（roles/permissions）
 * 不会被 {@code JacksonTypeHandler} 反序列化；这里改用 BaseMapper 查询以复用实体映射。</p>
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * @param username 登录名
     * @return 用户（含 roles / permissions）
     */
    default UserEntity selectByUsername(String username) {
        return selectOne(Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getUsername, username)
                .last("LIMIT 1"));
    }
}
