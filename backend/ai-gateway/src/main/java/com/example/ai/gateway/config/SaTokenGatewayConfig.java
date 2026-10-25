package com.example.ai.gateway.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

/**
 * 网关层登录校验（细粒度权限在各业务服务 {@code @SaCheckPermission}）。
 *
 * <p>Sa-Token 默认错误策略会抛出异常，被 Spring 兜底成 500；这里显式写回
 * 401/403，前端据此清理会话并跳转登录页。</p>
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
@Configuration
public class SaTokenGatewayConfig {

    private final ObjectMapper objectMapper;

    public SaTokenGatewayConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * @return Sa-Token 全局过滤器
     */
    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude(
                        "/auth/login",
                        "/auth/register",
                        "/actuator/**"
                )
                .setAuth(obj -> SaRouter.match("/**", r -> StpUtil.checkLogin()))
                .setError(this::writeError);
    }

    /**
     * 把鉴权异常写成统一 JSON，并设置正确的 HTTP 状态码。
     *
     * @param ex 鉴权异常
     * @return 响应体 JSON
     */
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
