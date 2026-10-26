package com.example.ai.common.security;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 注册 Sa-Token 权限数据源（各 Servlet 服务共用）。
 */
@AutoConfiguration
@ConditionalOnClass(StpInterface.class)
public class SaTokenAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(StpInterface.class)
    public StpInterface stpInterface() {
        return new SessionStpInterface();
    }
}
