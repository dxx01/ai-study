package com.example.ai.llm.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.example.ai.llm.ratelimit.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

/**
 * REST 异常处理：对前端返回可读提示，对日志保留排查信息。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * @param ex 参数错误
     * @return 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * @param ex 参数校验失败
     * @return 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("参数校验失败");
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    /**
     * @param ex 未登录
     * @return 401
     */
    @ExceptionHandler(NotLoginException.class)
    public ProblemDetail notLogin(NotLoginException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "未登录或 token 已失效");
    }

    /**
     * @param ex 无权限
     * @return 403
     */
    @ExceptionHandler(NotPermissionException.class)
    public ProblemDetail notPermission(NotPermissionException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "无权限: " + ex.getPermission());
    }

    /**
     * @param ex 限流异常
     * @param request 当前请求
     * @return 429，带 Retry-After
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleRateLimit(RateLimitExceededException ex,
                                                         HttpServletRequest request) {
        log.warn("请求被限流 path={} retryAfter={}s", request.getRequestURI(), ex.getRetryAfterSeconds());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        detail.setTitle("请求过于频繁");
        detail.setProperty("code", "RATE_LIMITED");
        detail.setProperty("retryAfterSeconds", ex.getRetryAfterSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(detail);
    }

    /**
     * @param ex 模型 4xx（认证、参数、额度等）
     * @param request 当前请求
     * @return 502，带可读提示
     */
    @ExceptionHandler(NonTransientAiException.class)
    public ProblemDetail handleAiClientError(NonTransientAiException ex, HttpServletRequest request) {
        log.warn("模型调用失败 path={} message={}", request.getRequestURI(), ex.getMessage());
        String message = ex.getMessage() == null ? "" : ex.getMessage();
        String detail;
        if (message.contains("401") || message.contains("403")) {
            detail = "模型服务认证失败，请联系管理员检查 API Key";
        } else if (message.contains("429")) {
            detail = "模型服务繁忙，请稍后再试";
        } else {
            detail = "模型服务请求失败，请稍后重试";
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, detail);
    }

    /**
     * @param ex 模型 5xx / 网络异常
     * @param request 当前请求
     * @return 503
     */
    @ExceptionHandler(TransientAiException.class)
    public ProblemDetail handleAiTransient(TransientAiException ex, HttpServletRequest request) {
        log.warn("模型服务暂时不可用 path={} message={}", request.getRequestURI(), ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "模型服务暂时不可用，请稍后重试");
    }

    /**
     * @param ex 下游服务错误
     * @param request 当前请求
     * @return 502
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ProblemDetail handleDownstream(RestClientResponseException ex, HttpServletRequest request) {
        log.warn("下游服务调用失败 path={} status={}", request.getRequestURI(), ex.getStatusCode().value());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "依赖服务暂时不可用，请稍后重试");
    }

    /**
     * @param ex 未处理异常
     * @param request 当前请求
     * @return 500
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail internal(Exception ex, HttpServletRequest request) {
        log.error("未处理异常 path={}", request.getRequestURI(), ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "服务内部错误，请稍后重试");
    }
}
