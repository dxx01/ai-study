-- 启用/停用：VARCHAR ACTIVE/DISABLED → TINYINT 0/1（见 docs/standards/dictionaries.md）
SET NAMES utf8mb4;
USE ai_platform;

UPDATE sys_user SET status = IF(UPPER(CAST(status AS CHAR)) = 'DISABLED', 0, 1);
ALTER TABLE sys_user
    MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态：0=禁用，1=启用';

UPDATE sys_role SET status = IF(UPPER(CAST(status AS CHAR)) = 'DISABLED', 0, 1);
ALTER TABLE sys_role
    MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态：0=禁用，1=启用';

UPDATE sys_menu SET status = IF(UPPER(CAST(status AS CHAR)) = 'DISABLED', 0, 1);
ALTER TABLE sys_menu
    MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态：0=禁用，1=启用';
