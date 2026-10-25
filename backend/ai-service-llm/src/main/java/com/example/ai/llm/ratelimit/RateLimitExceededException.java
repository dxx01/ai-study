package com.example.ai.llm.ratelimit;

/**
 * 对话频率超限。
 *
 * @author 邓鑫鑫
 * @since 0.5.0
 */
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    /**
     * @param message 面向用户的提示
     * @param retryAfterSeconds 建议重试等待秒数
     */
    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
