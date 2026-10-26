package com.example.ai.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai.auth.domain.SysMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统菜单查询。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuEntity> {

    /**
     * @param userId 用户主键
     * @return 用户角色可见的菜单
     */
    @Select("""
            SELECT DISTINCT m.id, m.parent_id, m.name, m.path, m.icon, m.permission_code,
                            m.sort_order, m.visible, m.status
            FROM sys_menu m
            JOIN sys_role_menu rm ON rm.menu_id = m.id
            JOIN sys_user_role ur ON ur.role_id = rm.role_id
            JOIN sys_role r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND r.status = 1
              AND m.status = 1
              AND m.visible = 1
            ORDER BY m.parent_id, m.sort_order, m.id
            """)
    List<SysMenuEntity> selectVisibleByUser(@Param("userId") long userId);

    /**
     * @return 全部可见菜单（管理端）
     */
    @Select("""
            SELECT id, parent_id, name, path, icon, permission_code, sort_order, visible, status
            FROM sys_menu
            WHERE status = 1 AND visible = 1
            ORDER BY parent_id, sort_order, id
            """)
    List<SysMenuEntity> selectAllVisible();

    /**
     * @return 全部菜单（含隐藏/禁用，管理端编辑用）
     */
    @Select("""
            SELECT id, parent_id, name, path, icon, permission_code, sort_order, visible, status
            FROM sys_menu
            ORDER BY parent_id, sort_order, id
            """)
    List<SysMenuEntity> selectAll();
}
