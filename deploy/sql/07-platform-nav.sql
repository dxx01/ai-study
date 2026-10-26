-- 已有库：将侧栏从「健康助手后台」迁移为「AI 应用平台」导航（幂等，可重复执行）
SET NAMES utf8mb4;
USE ai_platform;

-- 隐藏旧版顶栏菜单（保留数据，避免破坏 role_menu 历史）
UPDATE sys_menu
SET visible = 0, status = 0, updated_at = NOW(6)
WHERE path IN ('/chat', '/system', '/system/users', '/system/roles', '/system/menus');

INSERT INTO sys_menu (parent_id, name, path, icon, permission_code, sort_order, visible, status, created_at, updated_at)
SELECT 0, '工作台', '/workspace', 'Odometer', NULL, 1, 1, 1, NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/workspace');

INSERT INTO sys_menu (parent_id, name, path, icon, permission_code, sort_order, visible, status, created_at, updated_at)
SELECT 0, '应用', '/apps', 'Grid', 'ai:chat', 2, 1, 1, NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/apps');

INSERT INTO sys_menu (parent_id, name, path, icon, permission_code, sort_order, visible, status, created_at, updated_at)
SELECT 0, '知识库', '/knowledge', 'FolderOpened', 'ai:knowledge:read', 3, 1, 1, NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/knowledge');

INSERT INTO sys_menu (parent_id, name, path, icon, permission_code, sort_order, visible, status, created_at, updated_at)
SELECT 0, '模型与提示词', '/platform/models', 'Cpu', 'sys:user:manage', 4, 1, 1, NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/platform/models');

INSERT INTO sys_menu (parent_id, name, path, icon, permission_code, sort_order, visible, status, created_at, updated_at)
SELECT 0, '评测与运行记录', '/platform/evals', 'DataLine', 'sys:user:manage', 5, 1, 1, NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/platform/evals');

INSERT INTO sys_menu (parent_id, name, path, icon, permission_code, sort_order, visible, status, created_at, updated_at)
SELECT 0, '平台管理', '/platform', 'Setting', NULL, 9, 1, 1, NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/platform');

INSERT INTO sys_menu (parent_id, name, path, icon, permission_code, sort_order, visible, status, created_at, updated_at)
SELECT p.id, '用户管理', '/platform/users', 'User', 'sys:user:manage', 1, 1, 1, NOW(6), NOW(6)
FROM sys_menu p
WHERE p.path = '/platform'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/platform/users');

INSERT INTO sys_menu (parent_id, name, path, icon, permission_code, sort_order, visible, status, created_at, updated_at)
SELECT p.id, '角色管理', '/platform/roles', 'UserFilled', 'sys:role:manage', 2, 1, 1, NOW(6), NOW(6)
FROM sys_menu p
WHERE p.path = '/platform'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/platform/roles');

INSERT INTO sys_menu (parent_id, name, path, icon, permission_code, sort_order, visible, status, created_at, updated_at)
SELECT p.id, '菜单管理', '/platform/menus', 'Menu', 'sys:menu:manage', 3, 1, 1, NOW(6), NOW(6)
FROM sys_menu p
WHERE p.path = '/platform'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/platform/menus');

-- 管理员：关联全部可见新菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.visible = 1 AND m.status = 1
WHERE r.code = 'ADMIN'
  AND m.path IN (
    '/workspace', '/apps', '/knowledge', '/platform/models', '/platform/evals',
    '/platform', '/platform/users', '/platform/roles', '/platform/menus'
  );

-- 普通用户：工作台 + 应用 + 知识库
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.path IN ('/workspace', '/apps', '/knowledge')
WHERE r.code = 'USER';
