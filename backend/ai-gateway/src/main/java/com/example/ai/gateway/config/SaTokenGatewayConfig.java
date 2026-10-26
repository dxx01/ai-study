package com.example.ai.gateway.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

/**
 * 网关统一登录校验：白名单免登录，黑名单直接拒绝，其余路径必须已登录。
 *
 * <p>细粒度 {@code @SaCheckPermission} 仍在各业务服务；网关只挡未登录流量。</p>
 */
@Configuration
@EnableConfigurationProperties(GatewayAuthProperties.class)
public class SaTokenGatewayConfig {

    private final ObjectMapper objectMapper;
    private final GatewayAuthProperties gatewayAuthProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public SaTokenGatewayConfig(ObjectMapper objectMapper, GatewayAuthProperties gatewayAuthProperties) {
        this.objectMapper = objectMapper;
        this.gatewayAuthProperties = gatewayAuthProperties;
    }

    @Bean
    public SaReactorFilter saReactorFilter() {
        String[] whitelist = gatewayAuthProperties.getWhitelist().toArray(String[]::new);
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude(whitelist)
                .setAuth(obj -> SaRouter.match("/**", r -> {
                    String path = currentPath();
                    if (matchesAny(path, gatewayAuthProperties.getBlacklist())) {
                        throw new NotPermissionException("blacklist", "禁止访问");
                    }
                    StpUtil.checkLogin();
                }))
                .setError(this::writeError);
    }

    private String currentPath() {
        ServerWebExchange exchange = SaReactorSyncHolder.getContext();
        if (exchange == null) {
            return "";
        }
        return exchange.getRequest().getURI().getPath();
    }

    private boolean matchesAny(String path, Iterable<String> patterns) {
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String writeError(Throwable ex) {
        HttpStatus status = resolveStatus(ex);
        String detail = status == HttpStatus.UNAUTHORIZED
                ? "未登录或 token 已失效"
                : status == HttpStatus.FORBIDDEN ? "无权限访问" : "网关内部错误";

        ServerWebExchange exchange = SaReactorSyncHolder.getContext();
        if (exchange != null) {
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        }
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "status", status.value(),
                    "detail", detail
            ));
        } catch (JsonProcessingException ignored) {
            return "{\"status\":500,\"detail\":\"网关内部错误\"}";
        }
    }

    private static HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof NotLoginException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof NotPermissionException) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
