-- 业务表结构（与 docs/database/schema.md、MyBatis-Plus 实体一致）
-- 时间语义：Asia/Shanghai，由应用写入 DATETIME(6)
USE ai_platform;

CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键，与 Sa-Token loginId（数值）一致',
    username        VARCHAR(64)     NOT NULL COMMENT '登录名，系统内唯一',
    password_hash   VARCHAR(128)    NOT NULL COMMENT 'BCrypt 密码哈希',
    roles           JSON            NOT NULL COMMENT '角色列表',
    permissions     JSON            NOT NULL COMMENT '权限码列表',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区，应用写入）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='平台用户（ai-auth）';

CREATE TABLE IF NOT EXISTS chat_session (
    id              CHAR(36)        NOT NULL COMMENT '会话 UUID',
    owner_user_id   BIGINT          NOT NULL COMMENT '所属用户 ID，对应 sys_user.id（逻辑关联）',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    updated_at      DATETIME(6)     NOT NULL COMMENT '最后更新时间（上海时区）',
    PRIMARY KEY (id),
    KEY idx_chat_session_owner_updated (owner_user_id, updated_at DESC)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='对话会话';

CREATE TABLE IF NOT EXISTS chat_message (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '消息主键',
    session_id      CHAR(36)        NOT NULL COMMENT '所属会话',
    role            VARCHAR(16)     NOT NULL COMMENT 'user / assistant',
    content         MEDIUMTEXT      NOT NULL COMMENT '消息正文',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    PRIMARY KEY (id),
    KEY idx_chat_message_session_created_id (session_id, created_at, id),
    CONSTRAINT fk_chat_message_session
        FOREIGN KEY (session_id) REFERENCES chat_session (id)
        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='对话消息';
