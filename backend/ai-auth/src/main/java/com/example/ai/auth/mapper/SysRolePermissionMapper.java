package com.example.ai.auth.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色-权限关联。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@Mapper
public interface SysRolePermissionMapper {

    /**
     * @param roleId 角色主键
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deleteByRole(@Param("roleId") long roleId);

    /**
     * @param roleId 角色主键
     * @param permissionId 权限主键
     */
    @Insert("INSERT INTO sys_role_permission (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    void insert(@Param("roleId") long roleId, @Param("permissionId") long permissionId);
}
