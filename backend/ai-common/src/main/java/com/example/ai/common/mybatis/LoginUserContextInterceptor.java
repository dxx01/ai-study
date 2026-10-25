package com.example.ai.common.mybatis;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 将 Sa-Token 登录 ID 写入 {@link LoginUserContext}，供自动填充使用。
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
public class LoginUserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        if (StpUtil.isLogin()) {
            LoginUserContext.setUserId(StpUtil.getLoginIdAsLong());
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        LoginUserContext.clear();
    }
}
