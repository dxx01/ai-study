package com.example.ai.auth.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色-菜单关联。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@Mapper
public interface SysRoleMenuMapper {

    /**
     * @param roleId 角色主键
     * @param menuId 菜单主键
     */
    @Insert("INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (#{roleId}, #{menuId})")
    void insert(@Param("roleId") long roleId, @Param("menuId") long menuId);
}
