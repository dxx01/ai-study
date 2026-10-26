-- v0.7 增量：修复 v0.6 之前旧表/旧列的中文注释乱码（不改数据）
-- 仅修改注释与等价的列定义；执行一次即可
SET NAMES utf8mb4;
USE ai_platform;

ALTER TABLE sys_user
    MODIFY COLUMN id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，与 Sa-Token loginId（数值）一致',
    MODIFY COLUMN username      VARCHAR(64)  NOT NULL COMMENT '登录名，系统内唯一',
    MODIFY COLUMN password_hash VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码哈希',
    MODIFY COLUMN roles         JSON         NOT NULL COMMENT '角色列表',
    MODIFY COLUMN permissions   JSON         NOT NULL COMMENT '权限码列表',
    MODIFY COLUMN created_at    DATETIME(6)  NOT NULL COMMENT '创建时间（上海时区，应用写入）',
    COMMENT='平台用户（ai-auth）';

ALTER TABLE chat_session
    MODIFY COLUMN id            CHAR(36)    NOT NULL COMMENT '会话 UUID',
    MODIFY COLUMN owner_user_id BIGINT      NOT NULL COMMENT '所属用户 ID，对应 sys_user.id（逻辑关联）',
    MODIFY COLUMN created_at    DATETIME(6) NOT NULL COMMENT '创建时间（上海时区）',
    MODIFY COLUMN updated_at    DATETIME(6) NOT NULL COMMENT '最后更新时间（上海时区）',
    COMMENT='对话会话';

ALTER TABLE chat_message
    MODIFY COLUMN id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '消息主键',
    MODIFY COLUMN session_id CHAR(36)    NOT NULL COMMENT '所属会话',
    MODIFY COLUMN role       VARCHAR(16) NOT NULL COMMENT 'user / assistant',
    MODIFY COLUMN content    MEDIUMTEXT  NOT NULL COMMENT '消息正文',
    MODIFY COLUMN created_at DATETIME(6) NOT NULL COMMENT '创建时间（上海时区）',
    COMMENT='对话消息';
