package com.example.ai.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册登录用户上下文拦截器（与 Sa-Token 配合）。
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
@Configuration
@ConditionalOnClass(MetaObjectHandler.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class MybatisPlusWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginUserContextInterceptor())
                .addPathPatterns("/**")
                .order(10);
    }
}
