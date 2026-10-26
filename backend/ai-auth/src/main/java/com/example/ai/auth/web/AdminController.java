package com.example.ai.auth.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.ai.auth.service.AdminService;
import com.example.ai.auth.web.dto.AdminPermissionItem;
import com.example.ai.auth.web.dto.AdminRoleItem;
import com.example.ai.auth.web.dto.AdminUserItem;
import com.example.ai.auth.web.dto.MenuRequest;
import com.example.ai.auth.web.dto.UpdateRolePermissionsRequest;
import com.example.ai.auth.web.dto.UpdateUserRolesRequest;
import com.example.ai.auth.web.dto.UpdateUserStatusRequest;
import com.example.ai.common.api.dto.MenuItem;
import com.example.ai.common.security.AiPermissions;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端 API（用户 / 角色 / 权限 / 菜单）。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * @return 用户列表
     */
    @SaCheckPermission(AiPermissions.SYS_USER_MANAGE)
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AdminUserItem> users() {
        return adminService.users();
    }

    /**
     * @param id 用户主键
     * @param request 状态
     */
    @SaCheckPermission(AiPermissions.SYS_USER_MANAGE)
    @PutMapping(value = "/users/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateUserStatus(@PathVariable long id,
                                 @Valid @RequestBody UpdateUserStatusRequest request) {
        adminService.updateUserStatus(id, request.status());
    }

    /**
     * @param id 用户主键
     * @param request 角色编码
     */
    @SaCheckPermission(AiPermissions.SYS_USER_MANAGE)
    @PutMapping(value = "/users/{id}/roles", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateUserRoles(@PathVariable long id,
                                @Valid @RequestBody UpdateUserRolesRequest request) {
        adminService.updateUserRoles(id, request.roles());
    }

    /**
     * @return 角色列表
     */
    @SaCheckPermission(AiPermissions.SYS_ROLE_MANAGE)
    @GetMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AdminRoleItem> roles() {
        return adminService.roles();
    }

    /**
     * @param id 角色主键
     * @param request 权限码
     */
    @SaCheckPermission(AiPermissions.SYS_ROLE_MANAGE)
    @PutMapping(value = "/roles/{id}/permissions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateRolePermissions(@PathVariable long id,
                                      @Valid @RequestBody UpdateRolePermissionsRequest request) {
        adminService.updateRolePermissions(id, request.permissions());
    }

    /**
     * @return 权限选项
     */
    @SaCheckPermission(AiPermissions.SYS_ROLE_MANAGE)
    @GetMapping(value = "/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AdminPermissionItem> permissions() {
        return adminService.permissions();
    }

    /**
     * @return 菜单树
     */
    @SaCheckPermission(AiPermissions.SYS_MENU_MANAGE)
    @GetMapping(value = "/menus", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MenuItem> menus() {
        return adminService.menus();
    }

    /**
     * @param request 菜单
     */
    @SaCheckPermission(AiPermissions.SYS_MENU_MANAGE)
    @PostMapping(value = "/menus", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createMenu(@Valid @RequestBody MenuRequest request) {
        adminService.createMenu(request);
    }

    /**
     * @param id 菜单主键
     * @param request 菜单
     */
    @SaCheckPermission(AiPermissions.SYS_MENU_MANAGE)
    @PutMapping(value = "/menus/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateMenu(@PathVariable long id, @Valid @RequestBody MenuRequest request) {
        adminService.updateMenu(id, request);
    }

    /**
     * @param id 菜单主键
     */
    @SaCheckPermission(AiPermissions.SYS_MENU_MANAGE)
    @DeleteMapping("/menus/{id}")
    public void deleteMenu(@PathVariable long id) {
        adminService.deleteMenu(id);
    }
}
