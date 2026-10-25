package com.example.ai.common.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 从 Sa-Token Session（Redis）解析权限/角色，供各微服务 StpInterface 复用。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
public final class SessionPermissionResolver {

    private SessionPermissionResolver() {
    }

    /**
     * @param loginId 登录 ID
     * @return 权限列表
     */
    public static List<String> permissions(String loginId) {
        List<String> fromSession = readStringList(loginId, SaTokenSessionKeys.PERMISSIONS);
        return fromSession;
    }

    /**
     * @param loginId 登录 ID
     * @return 角色列表
     */
    public static List<String> roles(String loginId) {
        List<String> fromSession = readStringList(loginId, SaTokenSessionKeys.ROLES);
        return fromSession;
    }

    @SuppressWarnings("unchecked")
    private static List<String> readStringList(String loginId, String key) {
        try {
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            Object raw = session.get(key);
            if (raw instanceof List) {
                List<?> list = (List<?>) raw;
                List<String> result = new ArrayList<>();
                for (Object item : list) {
                    if (item != null) {
                        result.add(String.valueOf(item));
                    }
                }
                return result;
            }
        } catch (Exception ignored) {
            // 未登录或 Session 不可用时返回空列表
        }
        return Collections.emptyList();
    }
}
