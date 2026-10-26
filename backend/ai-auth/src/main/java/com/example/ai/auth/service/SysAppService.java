package com.example.ai.auth.service;

import com.example.ai.common.api.dto.PlatformAppSummary;
import com.example.ai.auth.domain.SysAppEntity;
import com.example.ai.auth.mapper.SysAppMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 平台应用查询。
 *
 * @author 邓鑫鑫
 * @since 0.8.0
 */
@Service
public class SysAppService {

    private final SysAppMapper appMapper;

    public SysAppService(SysAppMapper appMapper) {
        this.appMapper = appMapper;
    }

    /**
     * @return 已启用应用列表
     */
    public List<PlatformAppSummary> listEnabled() {
        return appMapper.selectEnabled().stream().map(SysAppService::toSummary).toList();
    }

    /**
     * @param code 应用标识
     * @return 是否存在且启用
     */
    public boolean isEnabled(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return appMapper.selectEnabledByCode(code.trim()) != null;
    }

    private static PlatformAppSummary toSummary(SysAppEntity entity) {
        return new PlatformAppSummary(
                entity.getCode(),
                entity.getName(),
                entity.getTagline(),
                entity.getDescription(),
                entity.getExpertId()
        );
    }
}
