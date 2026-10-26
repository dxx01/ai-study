package com.example.ai.common.security;

import cn.dev33.satoken.stp.StpInterface;
import java.util.List;

/**
 * Sa-Token 权限/角色数据源（从 Redis Session 读取，登录时由 ai-auth 写入）。
 */
public class SessionStpInterface implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return SessionPermissionResolver.permissions(String.valueOf(loginId));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return SessionPermissionResolver.roles(String.valueOf(loginId));
    }
}
