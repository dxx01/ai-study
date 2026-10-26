package com.example.ai.auth.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统 RBAC 查询（角色/权限）。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@Mapper
public interface SysRbacMapper {

    /**
     * @param userId 用户主键
     * @return 启用角色编码
     */
    @Select("""
            SELECT DISTINCT r.code
            FROM sys_role r
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND r.status = 1
            ORDER BY r.code
            """)
    List<String> selectRoleCodes(@Param("userId") long userId);

    /**
     * @param userId 用户主键
     * @return 启用角色的权限码
     */
    @Select("""
            SELECT DISTINCT p.code
            FROM sys_permission p
            JOIN sys_role_permission rp ON rp.permission_id = p.id
            JOIN sys_user_role ur ON ur.role_id = rp.role_id
            JOIN sys_role r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId} AND r.status = 1
            ORDER BY p.code
            """)
    List<String> selectPermissionCodes(@Param("userId") long userId);

    /**
     * 新用户绑定默认 USER 角色（幂等）。
     *
     * @param userId 用户主键
     * @return 影响行数
     */
    @Insert("""
            INSERT INTO sys_user_role (user_id, role_id)
            SELECT #{userId}, r.id
            FROM sys_role r
            WHERE r.code = 'USER'
              AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = #{userId})
            """)
    int assignDefaultRole(@Param("userId") long userId);
}
