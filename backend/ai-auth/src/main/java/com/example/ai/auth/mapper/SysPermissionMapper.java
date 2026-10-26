package com.example.ai.auth.mapper;

import com.example.ai.auth.domain.SysPermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限查询。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@Mapper
public interface SysPermissionMapper {

    /**
     * @param roleId 角色主键
     * @return 权限码
     */
    @Select("""
            SELECT p.code
            FROM sys_permission p
            JOIN sys_role_permission rp ON rp.permission_id = p.id
            WHERE rp.role_id = #{roleId}
            ORDER BY p.code
            """)
    List<String> selectCodesByRole(@Param("roleId") long roleId);

    /**
     * @return 全部权限
     */
    @Select("SELECT id, code, name, group_name FROM sys_permission ORDER BY code")
    List<SysPermissionEntity> selectAll();

    /**
     * @param code 权限码
     * @return 权限
     */
    @Select("SELECT id, code, name, group_name FROM sys_permission WHERE code = #{code}")
    SysPermissionEntity selectByCode(@Param("code") String code);
}
