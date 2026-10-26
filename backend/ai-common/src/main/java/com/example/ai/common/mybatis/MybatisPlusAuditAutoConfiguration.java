package com.example.ai.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * 仅在有 MyBatis-Plus 的服务中注册审计填充；网关等排除 mybatis 的模块不会加载。
 */
@AutoConfiguration
@ConditionalOnClass(MetaObjectHandler.class)
public class MybatisPlusAuditAutoConfiguration {

    @Bean
    public MetaObjectHandler auditMetaObjectHandler() {
        return new AuditMetaObjectHandler();
    }
}
