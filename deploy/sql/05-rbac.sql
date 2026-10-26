-- v0.7 增量：系统权限表（已有库执行一次；新库直接用 02-schema.sql）
-- 执行后运行 03-seed-data.sql（幂等）写入角色/权限/菜单与关联
SET NAMES utf8mb4;
USE ai_platform;

CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '角色主键',
    code            VARCHAR(64)     NOT NULL COMMENT '角色编码：ADMIN / USER',
    name            VARCHAR(64)     NOT NULL COMMENT '角色名称',
    description     VARCHAR(255)    NULL COMMENT '角色说明',
    status          VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
    sort_order      INT             NOT NULL DEFAULT 0 COMMENT '排序',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
                                    COMMENT '最后更新时间（上海时区）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限码';

CREATE TABLE IF NOT EXISTS sys_menu (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '菜单主键',
    parent_id       BIGINT          NOT NULL DEFAULT 0 COMMENT '父菜单 ID，0 为根',
    name            VARCHAR(64)     NOT NULL COMMENT '菜单名称',
    path            VARCHAR(128)    NOT NULL COMMENT '前端路由',
    icon            VARCHAR(64)     NULL COMMENT '图标',
    permission_code VARCHAR(128)    NULL COMMENT '进入菜单所需权限码，可空',
    sort_order      INT             NOT NULL DEFAULT 0 COMMENT '排序',
    visible         TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否可见',
    status          VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
    created_at      DATETIME(6)     NOT NULL COMMENT '创建时间（上海时区）',
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
                                    COMMENT '最后更新时间（上海时区）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_path (path),
    KEY idx_sys_menu_parent_sort (parent_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单';

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id         BIGINT          NOT NULL COMMENT '用户 ID',
    role_id         BIGINT          NOT NULL COMMENT '角色 ID',
    PRIMARY KEY (user_id, role_id),
    KEY idx_sys_user_role_role (role_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id         BIGINT          NOT NULL COMMENT '角色 ID',
    permission_id   BIGINT          NOT NULL COMMENT '权限 ID',
    PRIMARY KEY (role_id, permission_id),
    KEY idx_sys_role_permission_permission (permission_id),
    CONSTRAINT fk_sys_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id         BIGINT          NOT NULL COMMENT '角色 ID',
    menu_id         BIGINT          NOT NULL COMMENT '菜单 ID',
    PRIMARY KEY (role_id, menu_id),
    KEY idx_sys_role_menu_menu (menu_id),
    CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-菜单';
