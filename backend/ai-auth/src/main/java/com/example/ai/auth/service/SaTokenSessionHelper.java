package com.example.ai.auth.service;

import com.example.ai.auth.domain.UserRecord;
import com.example.ai.common.security.SaTokenSessionKeys;
import cn.dev33.satoken.stp.StpUtil;

/**
 * 登录后将权限写入 Sa-Token Session，供网关与 expert 服务读取。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
public final class SaTokenSessionHelper {

    private SaTokenSessionHelper() {
    }

    /**
     * @param user 已认证用户
     */
    public static void bindUserSession(UserRecord user) {
        StpUtil.login(user.id());
        StpUtil.getSession().set(SaTokenSessionKeys.USERNAME, user.username());
        StpUtil.getSession().set(SaTokenSessionKeys.ROLES, user.roles());
        StpUtil.getSession().set(SaTokenSessionKeys.PERMISSIONS, user.permissions());
    }
}
