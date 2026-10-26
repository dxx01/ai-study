package com.example.ai.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai.auth.domain.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {
}
