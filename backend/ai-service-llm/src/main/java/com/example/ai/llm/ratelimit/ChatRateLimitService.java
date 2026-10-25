package com.example.ai.llm.ratelimit;

import com.example.ai.llm.config.LlmProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 基于 Redis 的用户对话限流：分钟级 + 天级。
 *
 * <p>Redis 不可用时只记录告警并放行，避免限流组件拖垮主链路。</p>
 *
 * @author 邓鑫鑫
 * @since 0.5.0
 */
@Service
public class ChatRateLimitService {

    private static final Logger log = LoggerFactory.getLogger(ChatRateLimitService.class);

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final String MINUTE_KEY_PREFIX = "rate:chat:min:";
    private static final String DAY_KEY_PREFIX = "rate:chat:day:";

    private final StringRedisTemplate redis;
    private final LlmProperties properties;

    public ChatRateLimitService(StringRedisTemplate redis, LlmProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    /**
     * @param userId 当前登录用户主键
     */
    public void check(Long userId) {
        LlmProperties.RateLimit config = properties.getRateLimit();
        if (!config.isEnabled()) {
            return;
        }
        try {
            checkMinute(userId, config);
            checkDay(userId, config);
        } catch (RateLimitExceededException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.warn("限流检查跳过：Redis 不可用，userId={}", userId, ex);
        }
    }

    private void checkMinute(Long userId, LlmProperties.RateLimit config) {
        long now = System.currentTimeMillis();
        long minute = now / 60_000;
        String key = MINUTE_KEY_PREFIX + userId + ":" + minute;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofMinutes(2));
        }
        if (count != null && count > config.getChatPerMinute()) {
            long retryAfter = Math.max(1, 60 - (now / 1000) % 60);
            log.warn("对话触发分钟限流 userId={} count={} limit={}", userId, count, config.getChatPerMinute());
            throw new RateLimitExceededException(
                    "请求过于频繁，请 " + retryAfter + " 秒后再试", retryAfter);
        }
    }

    private void checkDay(Long userId, LlmProperties.RateLimit config) {
        String day = LocalDate.now(SHANGHAI).toString();
        String key = DAY_KEY_PREFIX + userId + ":" + day;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofDays(2));
        }
        if (count != null && count > config.getChatPerDay()) {
            long retryAfter = secondsUntilTomorrow();
            log.warn("对话触发每日限流 userId={} count={} limit={}", userId, count, config.getChatPerDay());
            throw new RateLimitExceededException(
                    "今日对话次数已达上限（" + config.getChatPerDay() + " 次），请明天再试", retryAfter);
        }
    }

    private static long secondsUntilTomorrow() {
        LocalDateTime now = LocalDateTime.now(SHANGHAI);
        LocalDateTime tomorrow = now.toLocalDate().plusDays(1).atStartOfDay();
        return Math.max(1, Duration.between(now, tomorrow).getSeconds());
    }
}
