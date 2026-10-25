package com.example.ai.auth.config;

import cn.dev33.satoken.stp.StpInterface;
import com.example.ai.common.security.SessionPermissionResolver;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限/角色数据源（从 Redis Session 读取，登录时已写入）。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
@Component
public class SaTokenStpInterface implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return SessionPermissionResolver.permissions(String.valueOf(loginId));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return SessionPermissionResolver.roles(String.valueOf(loginId));
    }
}
