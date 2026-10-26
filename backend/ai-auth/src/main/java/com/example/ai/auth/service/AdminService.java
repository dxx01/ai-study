package com.example.ai.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.ai.auth.domain.SysMenuEntity;
import com.example.ai.auth.domain.SysPermissionEntity;
import com.example.ai.auth.domain.SysRoleEntity;
import com.example.ai.auth.domain.UserEntity;
import com.example.ai.auth.mapper.SysMenuMapper;
import com.example.ai.auth.mapper.SysPermissionMapper;
import com.example.ai.auth.mapper.SysRbacMapper;
import com.example.ai.auth.mapper.SysRoleMapper;
import com.example.ai.auth.mapper.SysRoleMenuMapper;
import com.example.ai.auth.mapper.SysRolePermissionMapper;
import com.example.ai.auth.mapper.SysUserRoleMapper;
import com.example.ai.auth.mapper.UserMapper;
import com.example.ai.auth.web.dto.AdminPermissionItem;
import com.example.ai.auth.web.dto.AdminRoleItem;
import com.example.ai.auth.web.dto.AdminUserItem;
import com.example.ai.auth.web.dto.MenuRequest;
import com.example.ai.common.api.dto.MenuItem;
import com.example.ai.common.dict.EnableStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理端查询与维护。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@Service
public class AdminService {

    private final UserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRbacMapper rbacMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuService menuService;

    public AdminService(UserMapper userMapper,
                        SysRoleMapper roleMapper,
                        SysPermissionMapper permissionMapper,
                        SysRbacMapper rbacMapper,
                        SysMenuMapper menuMapper,
                        SysUserRoleMapper userRoleMapper,
                        SysRolePermissionMapper rolePermissionMapper,
                        SysRoleMenuMapper roleMenuMapper,
                        SysMenuService menuService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rbacMapper = rbacMapper;
        this.menuMapper = menuMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuService = menuService;
    }

    /**
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    public List<AdminUserItem> users() {
        List<UserEntity> users = userMapper.selectList(
                Wrappers.<UserEntity>lambdaQuery().orderByAsc(UserEntity::getId));
        return users.stream()
                .map(user -> new AdminUserItem(
                        user.getId(),
                        user.getUsername(),
                        user.getStatus(),
                        rolesOf(user.getId()),
                        user.getLastLoginAt(),
                        user.getCreatedAt()))
                .toList();
    }

    /**
     * @return 角色列表（含权限码）
     */
    @Transactional(readOnly = true)
    public List<AdminRoleItem> roles() {
        List<SysRoleEntity> roles = roleMapper.selectList(
                Wrappers.<SysRoleEntity>lambdaQuery().orderByAsc(SysRoleEntity::getSortOrder));
        return roles.stream()
                .map(role -> new AdminRoleItem(
                        role.getId(),
                        role.getCode(),
                        role.getName(),
                        role.getStatus(),
                        role.getSortOrder(),
                        permissionsOf(role.getId())))
                .toList();
    }

    /**
     * @return 全部权限选项
     */
    @Transactional(readOnly = true)
    public List<AdminPermissionItem> permissions() {
        return permissionMapper.selectAll().stream()
                .map(item -> new AdminPermissionItem(item.getId(), item.getCode(), item.getName(), item.getGroupName()))
                .toList();
    }

    /**
     * @return 全部可见菜单树
     */
    public List<MenuItem> menus() {
        return menuService.allMenus();
    }

    /**
     * @param userId 用户主键
     * @param status 0=禁用，1=启用
     */
    @Transactional
    public void updateUserStatus(long userId, int status) {
        if (userId == StpUtil.getLoginIdAsLong() && status == EnableStatus.DISABLED) {
            throw new IllegalArgumentException("不能禁用当前登录账号");
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        userMapper.update(null, Wrappers.<UserEntity>lambdaUpdate()
                .eq(UserEntity::getId, userId)
                .set(UserEntity::getStatus, status));
    }

    /**
     * @param userId 用户主键
     * @param roleCodes 角色编码
     */
    @Transactional
    public void updateUserRoles(long userId, List<String> roleCodes) {
        if (userMapper.selectById(userId) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        List<String> codes = roleCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(code -> code.trim().toUpperCase())
                .distinct()
                .toList();
        List<SysRoleEntity> roles = codes.stream()
                .map(code -> roleMapper.selectOne(Wrappers.<SysRoleEntity>lambdaQuery()
                        .eq(SysRoleEntity::getCode, code)))
                .filter(role -> role != null)
                .toList();
        if (roles.size() != codes.size()) {
            throw new IllegalArgumentException("存在未知角色");
        }
        if (userId == StpUtil.getLoginIdAsLong()
                && roles.stream().noneMatch(role -> "ADMIN".equals(role.getCode()))) {
            throw new IllegalArgumentException("不能移除自己的管理员角色");
        }
        userRoleMapper.deleteByUser(userId);
        for (SysRoleEntity role : roles) {
            userRoleMapper.insert(userId, role.getId());
        }
    }

    /**
     * @param roleId 角色主键
     * @param permissionCodes 权限码
     */
    @Transactional
    public void updateRolePermissions(long roleId, List<String> permissionCodes) {
        if (roleMapper.selectById(roleId) == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        List<String> codes = permissionCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        List<SysPermissionEntity> permissions = codes.stream()
                .map(permissionMapper::selectByCode)
                .filter(item -> item != null)
                .toList();
        if (permissions.size() != codes.size()) {
            throw new IllegalArgumentException("存在未知权限码");
        }
        rolePermissionMapper.deleteByRole(roleId);
        for (SysPermissionEntity permission : permissions) {
            rolePermissionMapper.insert(roleId, permission.getId());
        }
    }

    /**
     * @param request 菜单
     */
    @Transactional
    public void createMenu(MenuRequest request) {
        validateParent(request.parentId());
        ensurePathAvailable(request.path(), null);
        SysMenuEntity entity = toEntity(null, request);
        try {
            menuMapper.insert(entity);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("菜单路由已存在");
        }
        SysRoleEntity admin = roleMapper.selectOne(Wrappers.<SysRoleEntity>lambdaQuery()
                .eq(SysRoleEntity::getCode, "ADMIN"));
        if (admin != null) {
            roleMenuMapper.insert(admin.getId(), entity.getId());
        }
    }

    /**
     * @param id 菜单主键
     * @param request 菜单
     */
    @Transactional
    public void updateMenu(long id, MenuRequest request) {
        if (menuMapper.selectById(id) == null) {
            throw new IllegalArgumentException("菜单不存在");
        }
        validateParent(request.parentId());
        ensurePathAvailable(request.path(), id);
        try {
            menuMapper.updateById(toEntity(id, request));
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("菜单路由已存在");
        }
    }

    /**
     * @param id 菜单主键
     */
    @Transactional
    public void deleteMenu(long id) {
        if (menuMapper.selectById(id) == null) {
            throw new IllegalArgumentException("菜单不存在");
        }
        Long children = menuMapper.selectCount(Wrappers.<SysMenuEntity>lambdaQuery()
                .eq(SysMenuEntity::getParentId, id));
        if (children != null && children > 0) {
            throw new IllegalArgumentException("请先删除子菜单");
        }
        menuMapper.deleteById(id);
    }

    private void validateParent(Long parentId) {
        if (parentId != null && parentId != 0 && menuMapper.selectById(parentId) == null) {
            throw new IllegalArgumentException("父菜单不存在");
        }
    }

    private void ensurePathAvailable(String path, Long excludeId) {
        Long count = menuMapper.selectCount(Wrappers.<SysMenuEntity>lambdaQuery()
                .eq(SysMenuEntity::getPath, path)
                .ne(excludeId != null, SysMenuEntity::getId, excludeId));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("菜单路由已存在");
        }
    }

    private static SysMenuEntity toEntity(Long id, MenuRequest request) {
        SysMenuEntity entity = new SysMenuEntity();
        entity.setId(id);
        entity.setParentId(request.parentId() == null ? 0L : request.parentId());
        entity.setName(request.name());
        entity.setPath(request.path());
        entity.setIcon(request.icon());
        entity.setPermissionCode(request.permissionCode());
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setVisible(request.visible() == null || request.visible());
        entity.setStatus(request.status() == null ? EnableStatus.ENABLED : request.status());
        return entity;
    }

    private List<String> rolesOf(Long userId) {
        try {
            return rbacMapper.selectRoleCodes(userId);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private List<String> permissionsOf(Long roleId) {
        try {
            return permissionMapper.selectCodesByRole(roleId);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }
}
