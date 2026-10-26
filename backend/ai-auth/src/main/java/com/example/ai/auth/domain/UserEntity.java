package com.example.ai.auth.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import com.example.ai.common.dict.EnableStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 平台用户（表 sys_user）。
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
@TableName(value = "sys_user", autoResultMap = true)
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    @TableField("password_hash")
    private String passwordHash;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> roles = new ArrayList<>();

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> permissions = new ArrayList<>();

    private Integer status = 1;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return 领域只读视图（使用 JSON 兜底）
     */
    public UserRecord toRecord() {
        return toRecord(roles, permissions);
    }

    /**
     * @param resolvedRoles 角色编码
     * @param resolvedPermissions 权限码
     * @return 领域只读视图
     */
    public UserRecord toRecord(List<String> resolvedRoles, List<String> resolvedPermissions) {
        return new UserRecord(
                String.valueOf(id),
                username,
                passwordHash,
                resolvedRoles,
                resolvedPermissions,
                status == null ? EnableStatus.ENABLED : status
        );
    }
}
