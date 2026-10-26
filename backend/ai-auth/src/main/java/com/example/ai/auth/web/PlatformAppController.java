package com.example.ai.auth.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.ai.auth.service.SysAppService;
import com.example.ai.common.api.dto.PlatformAppSummary;
import com.example.ai.common.security.AiPermissions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台应用 API（登录用户可读）。
 *
 * @author 邓鑫鑫
 * @since 0.8.0
 */
@RestController
@RequestMapping("/api/apps")
public class PlatformAppController {

    private final SysAppService appService;

    public PlatformAppController(SysAppService appService) {
        this.appService = appService;
    }

    /**
     * @return 已启用应用列表
     */
    @SaCheckPermission(AiPermissions.CHAT)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PlatformAppSummary> list() {
        return appService.listEnabled();
    }

    /**
     * @param code 应用标识
     * @return 应用摘要
     */
    @SaCheckPermission(AiPermissions.CHAT)
    @GetMapping(value = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PlatformAppSummary get(@PathVariable String code) {
        return appService.listEnabled().stream()
                .filter(app -> app.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("应用不存在或已停用: " + code));
    }
}
