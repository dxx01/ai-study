-- v0.6 增量脚本（已有库执行一次；新库直接执行 02-schema.sql）
-- 目标：单租户、本地/单机服务器；不做多租户与迁移工具
SET NAMES utf8mb4;
USE ai_platform;

-- 用户：状态、最近登录、更新时间
ALTER TABLE sys_user
    ADD COLUMN status        VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
        COMMENT '账号状态：ACTIVE / DISABLED' AFTER permissions,
    ADD COLUMN last_login_at DATETIME(6) NULL
        COMMENT '最近登录时间（上海时区）' AFTER status,
    ADD COLUMN updated_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
        COMMENT '最后更新时间（上海时区）' AFTER created_at,
    ADD KEY idx_sys_user_status (status);

-- 会话：标题、消息数、最后消息时间
ALTER TABLE chat_session
    ADD COLUMN title           VARCHAR(128) NOT NULL DEFAULT '新对话'
        COMMENT '会话标题，首条用户消息可自动生成' AFTER owner_user_id,
    ADD COLUMN message_count   INT NOT NULL DEFAULT 0
        COMMENT '消息条数（冗余字段，便于列表展示）' AFTER title,
    ADD COLUMN last_message_at DATETIME(6) NULL
        COMMENT '最后一条消息时间（上海时区）' AFTER message_count;

-- 消息：幂等、状态、模型/Token/延迟/追踪、引用元数据
ALTER TABLE chat_message
    ADD COLUMN client_message_id VARCHAR(64) NULL
        COMMENT '客户端消息 ID，用于重试幂等；可为空' AFTER session_id,
    ADD COLUMN status            VARCHAR(16) NOT NULL DEFAULT 'COMPLETED'
        COMMENT 'PENDING / STREAMING / COMPLETED / FAILED' AFTER content,
    ADD COLUMN provider          VARCHAR(32) NULL
        COMMENT '模型提供方，如 deepseek' AFTER status,
    ADD COLUMN model             VARCHAR(64) NULL
        COMMENT '实际使用的模型名' AFTER provider,
    ADD COLUMN prompt_tokens     INT NULL COMMENT '输入 token 数' AFTER model,
    ADD COLUMN completion_tokens INT NULL COMMENT '输出 token 数' AFTER prompt_tokens,
    ADD COLUMN latency_ms        INT NULL COMMENT '模型调用耗时（毫秒）' AFTER completion_tokens,
    ADD COLUMN finish_reason     VARCHAR(32) NULL COMMENT '模型结束原因' AFTER latency_ms,
    ADD COLUMN trace_id          VARCHAR(64) NULL COMMENT '链路追踪 ID' AFTER finish_reason,
    ADD COLUMN metadata          JSON NULL COMMENT '引用/扩展元数据（如 sources）' AFTER trace_id,
    ADD UNIQUE KEY uk_chat_message_client (session_id, client_message_id),
    ADD KEY idx_chat_message_trace (trace_id);

-- 回填会话冗余字段
UPDATE chat_session s
LEFT JOIN (
    SELECT session_id, COUNT(*) AS cnt, MAX(created_at) AS last_at
    FROM chat_message
    GROUP BY session_id
) m ON m.session_id = s.id
SET s.message_count = COALESCE(m.cnt, 0),
    s.last_message_at = m.last_at;

-- 知识库文档与分块
CREATE TABLE IF NOT EXISTS knowledge_document (
    id              CHAR(36)        NOT NULL COMMENT '文档 UUID',
    owner_user_id   BIGINT          NOT NULL COMMENT '上传用户 ID，对应 sys_user.id（逻辑关联）',
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
