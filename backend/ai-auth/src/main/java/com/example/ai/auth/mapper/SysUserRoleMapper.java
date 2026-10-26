package com.example.ai.auth.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户-角色关联。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@Mapper
public interface SysUserRoleMapper {

    /**
     * @param userId 用户主键
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    void deleteByUser(@Param("userId") long userId);

    /**
     * @param userId 用户主键
     * @param roleId 角色主键
     */
    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insert(@Param("userId") long userId, @Param("roleId") long roleId);
}
