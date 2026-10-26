-- 已有库增量（只执行一次）：平台应用注册表 + 知识库按应用隔离
SET NAMES utf8mb4;
USE ai_platform;

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

INSERT INTO sys_app (code, name, tagline, description, expert_id, status, sort_order, created_at, updated_at)
SELECT 'health', '健康科普顾问', '示例应用',
       '居家营养与健康科普场景：本地知识库问答、引用溯源与资料不足拒答。用于练习 RAG、会话与评测链路。',
       'home-health-advisor', 1, 1, NOW(6), NOW(6)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_app WHERE code = 'health');

-- knowledge_document：按应用隔离（列不存在时添加）
SET @col_app := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'knowledge_document' AND COLUMN_NAME = 'app_id'
);
SET @sql_app := IF(@col_app = 0,
    'ALTER TABLE knowledge_document ADD COLUMN app_id VARCHAR(32) NOT NULL DEFAULT ''health'' COMMENT ''所属应用，对应 sys_app.code'' AFTER owner_user_id',
    'SELECT 1');
PREPARE stmt FROM @sql_app;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_cat := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'knowledge_document' AND COLUMN_NAME = 'category'
);
SET @sql_cat := IF(@col_cat = 0,
    'ALTER TABLE knowledge_document ADD COLUMN category TINYINT NOT NULL DEFAULT 0 COMMENT ''资料类型：0=未分类，1=食物营养，2=疾病科普，9=其他'' AFTER app_id',
    'SELECT 1');
PREPARE stmt FROM @sql_cat;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE knowledge_document SET app_id = 'health' WHERE app_id IS NULL OR app_id = '';

SET @idx1 := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'knowledge_document' AND INDEX_NAME = 'idx_knowledge_document_app_created'
);
SET @sql_idx1 := IF(@idx1 = 0,
    'CREATE INDEX idx_knowledge_document_app_created ON knowledge_document (app_id, created_at DESC)',
    'SELECT 1');
PREPARE stmt FROM @sql_idx1;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx2 := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'knowledge_document' AND INDEX_NAME = 'idx_knowledge_document_app_category'
);
SET @sql_idx2 := IF(@idx2 = 0,
    'CREATE INDEX idx_knowledge_document_app_category ON knowledge_document (app_id, category)',
    'SELECT 1');
PREPARE stmt FROM @sql_idx2;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
