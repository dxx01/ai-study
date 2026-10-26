package com.example.ai.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import java.time.LocalDateTime;

/**
 * 插入/更新时自动填充时间（上海时区）及当前登录用户 ID（若字段存在且未赋值）。
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";
    private static final String OWNER_USER_ID = "ownerUserId";
    private static final String CREATED_BY = "createdBy";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now(DbTimeZones.SHANGHAI);
        strictInsertFill(metaObject, CREATED_AT, LocalDateTime.class, now);
        strictInsertFill(metaObject, UPDATED_AT, LocalDateTime.class, now);

        LoginUserContext.getUserIdOptional().ifPresent(userId -> {
            fillIfNull(metaObject, OWNER_USER_ID, Long.class, userId);
            fillIfNull(metaObject, CREATED_BY, Long.class, userId);
        });
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, UPDATED_AT, LocalDateTime.class, LocalDateTime.now(DbTimeZones.SHANGHAI));
    }

    private <T> void fillIfNull(MetaObject metaObject, String field, Class<T> type, T value) {
        if (metaObject.hasSetter(field) && getFieldValByName(field, metaObject) == null) {
            strictInsertFill(metaObject, field, type, value);
        }
    }
}
