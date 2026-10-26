package com.example.ai.knowledge.service;

import com.example.ai.knowledge.mapper.SysAppReadMapper;
import org.springframework.stereotype.Service;

/**
 * 校验知识库操作所属应用是否已在平台注册且启用。
 *
 * @author 邓鑫鑫
 * @since 0.8.0
 */
@Service
public class PlatformAppValidator {

    public static final String DEFAULT_APP_ID = "health";

    private final SysAppReadMapper sysAppReadMapper;

    public PlatformAppValidator(SysAppReadMapper sysAppReadMapper) {
        this.sysAppReadMapper = sysAppReadMapper;
    }

    /**
     * @param appId 应用标识
     * @return 规范化后的 appId
     */
    public String requireEnabled(String appId) {
        String normalized = normalize(appId);
        if (sysAppReadMapper.selectEnabledFlag(normalized) == null) {
            throw new IllegalArgumentException("未知或已停用的应用: " + normalized);
        }
        return normalized;
    }

    public static String normalize(String appId) {
        if (appId == null || appId.isBlank()) {
            return DEFAULT_APP_ID;
        }
        return appId.trim();
    }
}
