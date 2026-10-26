-- 业务表结构（单租户；本地 / 单机服务器）
-- 时间语义：Asia/Shanghai，应用写入 DATETIME(6)
-- 编码：文件 UTF-8；客户端需 --default-character-set=utf8mb4（见 scripts/init-mysql-local.ps1）
SET NAMES utf8mb4;
USE ai_platform;

CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键，与 Sa-Token loginId（数值）一致',
    username        VARCHAR(64)     NOT NULL COMMENT '登录名，系统内唯一',
    password_hash   VARCHAR(128)    NOT NULL COMMENT 'BCrypt 密码哈希',
    roles           JSON            NOT NULL COMMENT '角色列表',
    permissions     JSON            NOT NULL COMMENT '权限码列表',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '启用状态：0=禁用，1=启用',
    last_login_at   DATETIME(6)     NULL COMMENT '最近登录时间（上海时区）',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区，应用写入）',
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
                                    COMMENT '最后更新时间（上海时区）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_status (status)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='平台用户（ai-auth）';

CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '角色主键',
    code            VARCHAR(64)     NOT NULL COMMENT '角色编码：ADMIN / USER',
    name            VARCHAR(64)     NOT NULL COMMENT '角色名称',
    description     VARCHAR(255)    NULL COMMENT '角色说明',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '启用状态：0=禁用，1=启用',
    sort_order      INT             NOT NULL DEFAULT 0 COMMENT '排序',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
                                    COMMENT '最后更新时间（上海时区）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='系统角色';

CREATE TABLE IF NOT EXISTS sys_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '权限主键',
    code            VARCHAR(128)    NOT NULL COMMENT '权限码，如 ai:knowledge:write',
    name            VARCHAR(64)     NOT NULL COMMENT '权限名称',
    group_name      VARCHAR(64)     NULL COMMENT '分组/模块',
    description     VARCHAR(255)    NULL COMMENT '说明',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
                                    COMMENT '最后更新时间（上海时区）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='系统权限码';

CREATE TABLE IF NOT EXISTS sys_menu (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '菜单主键',
    parent_id       BIGINT          NOT NULL DEFAULT 0 COMMENT '父菜单 ID，0 为根',
    name            VARCHAR(64)     NOT NULL COMMENT '菜单名称',
    path            VARCHAR(128)    NOT NULL COMMENT '前端路由',
    icon            VARCHAR(64)     NULL COMMENT '图标',
    permission_code VARCHAR(128)    NULL COMMENT '进入菜单所需权限码，可空',
    sort_order      INT             NOT NULL DEFAULT 0 COMMENT '排序',
    visible         TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否可见',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '启用状态：0=禁用，1=启用',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
                                    COMMENT '最后更新时间（上海时区）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_path (path),
    KEY idx_sys_menu_parent_sort (parent_id, sort_order)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='系统菜单';

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id         BIGINT          NOT NULL COMMENT '用户 ID',
    role_id         BIGINT          NOT NULL COMMENT '角色 ID',
    PRIMARY KEY (user_id, role_id),
    KEY idx_sys_user_role_role (role_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户-角色';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id         BIGINT          NOT NULL COMMENT '角色 ID',
    permission_id   BIGINT          NOT NULL COMMENT '权限 ID',
    PRIMARY KEY (role_id, permission_id),
    KEY idx_sys_role_permission_permission (permission_id),
    CONSTRAINT fk_sys_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='角色-权限';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id         BIGINT          NOT NULL COMMENT '角色 ID',
    menu_id         BIGINT          NOT NULL COMMENT '菜单 ID',
    PRIMARY KEY (role_id, menu_id),
    KEY idx_sys_role_menu_menu (menu_id),
    CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='角色-菜单';

CREATE TABLE IF NOT EXISTS sys_app (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '应用主键',
    code            VARCHAR(32)     NOT NULL COMMENT '应用标识，与路由 /apps/{code} 一致，如 health',
    name            VARCHAR(64)     NOT NULL COMMENT '应用名称',
    tagline         VARCHAR(128)    NULL COMMENT '副标题/标签',
    description     VARCHAR(512)    NULL COMMENT '应用说明',
    expert_id       VARCHAR(64)     NULL COMMENT '对话专家 ID，对应 LLM 编排配置',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '启用状态：0=禁用，1=启用',
    sort_order      INT             NOT NULL DEFAULT 0 COMMENT '列表排序',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
                                    COMMENT '最后更新时间（上海时区）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_app_code (code),
    KEY idx_sys_app_status_sort (status, sort_order)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='平台 AI 应用注册';

CREATE TABLE IF NOT EXISTS chat_session (
    id              CHAR(36)        NOT NULL COMMENT '会话 UUID',
    owner_user_id   BIGINT          NOT NULL COMMENT '所属用户 ID，对应 sys_user.id（逻辑关联）',
    title           VARCHAR(128)    NOT NULL DEFAULT '新对话' COMMENT '会话标题，首条用户消息可自动生成',
    message_count   INT             NOT NULL DEFAULT 0 COMMENT '消息条数（冗余字段，便于列表展示）',
    last_message_at DATETIME(6)     NULL COMMENT '最后一条消息时间（上海时区）',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
                                    COMMENT '最后更新时间（上海时区）',
    PRIMARY KEY (id),
    KEY idx_chat_session_owner_updated (owner_user_id, updated_at DESC)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='对话会话';

CREATE TABLE IF NOT EXISTS chat_message (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '消息主键',
    session_id          CHAR(36)        NOT NULL COMMENT '所属会话',
    client_message_id   VARCHAR(64)     NULL COMMENT '客户端消息 ID，用于重试幂等；可为空',
    role                VARCHAR(16)     NOT NULL COMMENT 'user / assistant',
    content             MEDIUMTEXT      NOT NULL COMMENT '消息正文',
    status              VARCHAR(16)     NOT NULL DEFAULT 'COMPLETED' COMMENT 'PENDING / STREAMING / COMPLETED / FAILED',
    provider            VARCHAR(32)     NULL COMMENT '模型提供方，如 deepseek',
    model               VARCHAR(64)     NULL COMMENT '实际使用的模型名',
    prompt_tokens       INT             NULL COMMENT '输入 token 数',
    completion_tokens   INT             NULL COMMENT '输出 token 数',
    latency_ms          INT             NULL COMMENT '模型调用耗时（毫秒）',
    finish_reason       VARCHAR(32)     NULL COMMENT '模型结束原因',
    trace_id            VARCHAR(64)     NULL COMMENT '链路追踪 ID',
    metadata            JSON            NULL COMMENT '引用/扩展元数据（如 sources）',
    created_at          DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_message_client (session_id, client_message_id),
    KEY idx_chat_message_session_created_id (session_id, created_at, id),
    KEY idx_chat_message_trace (trace_id),
    CONSTRAINT fk_chat_message_session
        FOREIGN KEY (session_id) REFERENCES chat_session (id)
        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='对话消息';

CREATE TABLE IF NOT EXISTS knowledge_document (
    id              CHAR(36)        NOT NULL COMMENT '文档 UUID',
    owner_user_id   BIGINT          NOT NULL COMMENT '上传用户 ID，对应 sys_user.id（逻辑关联）',
    app_id          VARCHAR(32)     NOT NULL COMMENT '所属应用，对应 sys_app.code',
    category        TINYINT         NOT NULL DEFAULT 0 COMMENT '资料类型：0=未分类，1=食物营养，2=疾病科普，9=其他',
    file_name       VARCHAR(255)    NOT NULL COMMENT '原始文件名',
    storage_path    VARCHAR(512)    NOT NULL COMMENT '本地存储路径',
    content_hash    CHAR(64)        NOT NULL COMMENT '内容 SHA-256（十六进制），用于去重/校验',
    size_bytes      BIGINT          NOT NULL COMMENT '文件大小（字节）',
    chunk_count     INT             NOT NULL DEFAULT 0 COMMENT '分块数量',
    embedding_model VARCHAR(64)     NULL COMMENT '生成向量所用 embedding 模型',
    status          VARCHAR(16)     NOT NULL DEFAULT 'INDEXED' COMMENT 'UPLOADING / INDEXED / FAILED',
    error_message   VARCHAR(512)    NULL COMMENT '失败原因',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
                                    COMMENT '最后更新时间（上海时区）',
    deleted_at      DATETIME(6)     NULL COMMENT '软删除时间；为空表示未删除',
    PRIMARY KEY (id),
    KEY idx_knowledge_document_owner_created (owner_user_id, created_at DESC),
    KEY idx_knowledge_document_app_created (app_id, created_at DESC),
    KEY idx_knowledge_document_app_category (app_id, category),
    KEY idx_knowledge_document_hash (content_hash)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='知识库文档元数据';

CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '分块主键',
    document_id     CHAR(36)        NOT NULL COMMENT '所属文档',
    chunk_index     INT             NOT NULL COMMENT '分块序号，从 0 开始',
    content         MEDIUMTEXT      NOT NULL COMMENT '分块正文',
    token_count     INT             NULL COMMENT '分块 token 数',
    vector_id       VARCHAR(128)    NULL COMMENT '向量库中的 ID',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_knowledge_chunk_doc_index (document_id, chunk_index),
    KEY idx_knowledge_chunk_document (document_id),
    CONSTRAINT fk_knowledge_chunk_document
        FOREIGN KEY (document_id) REFERENCES knowledge_document (id)
        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='知识库文档分块';
