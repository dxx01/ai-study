package com.example.ai.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关鉴权路径：白名单免登录，黑名单一律拒绝（即使已登录）。
 */
@ConfigurationProperties(prefix = "app.gateway.auth")
public class GatewayAuthProperties {

    /**
     * 免登录路径（Ant 风格），默认登录/注册与健康检查。
     */
    private List<String> whitelist = new ArrayList<>(List.of(
            "/auth/login",
            "/auth/register",
            "/actuator/**"
    ));

    /**
     * 禁止访问路径（Ant 风格），优先级高于白名单之外的业务校验。
     */
    private List<String> blacklist = new ArrayList<>();

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(List<String> blacklist) {
        this.blacklist = blacklist;
    }
}
