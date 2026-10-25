package com.example.ai.common.mybatis;

import java.time.ZoneId;

/**
 * 数据库业务时间时区（与 JDBC serverTimezone 一致）。
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
public final class DbTimeZones {

    /** 上海时区，用于 created_at / updated_at 自动填充 */
    public static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private DbTimeZones() {
    }
}
