-- 演示数据（幂等）：用户 + 角色 + 权限 + 菜单 + 关联
SET NAMES utf8mb4;
USE ai_platform;

-- 用户（密码哈希对应 admin123 / user123）
INSERT INTO sys_user (username, password_hash, roles, permissions, created_at)
SELECT 'admin', '$2a$10$hYblB87eO6gr8Hx3n334I.AJvbpFdjHh0E/G9ivY/mZAwkpi5Sbg2',
       JSON_ARRAY('admin'),
       JSON_ARRAY('ai:chat', 'ai:knowledge:read', 'ai:knowledge:write', 'ai:knowledge:admin',
                  'sys:user:manage', 'sys:role:manage', 'sys:menu:manage'),
       CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

INSERT INTO sys_user (username, password_hash, roles, permissions, created_at)
SELECT 'user', '$2a$10$ES3P14obVDPbAQkpPz2b/OhNflNB4cCz6SAaV95Q2vvYzUoqtUomy',
       JSON_ARRAY('user'),
       JSON_ARRAY('ai:chat', 'ai:knowledge:read'),
       CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'user');

-- 角色
INSERT INTO sys_role (code, name, description, status, sort_order, created_at, updated_at)
SELECT 'ADMIN', '管理员', '全部系统权限', 1, 1, NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'ADMIN');

INSERT INTO sys_role (code, name, description, status, sort_order, created_at, updated_at)
SELECT 'USER', '普通用户', '对话与知识库只读', 1, 2, NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'USER');

-- 权限码
INSERT INTO sys_permission (code, name, group_name, description, created_at, updated_at)
SELECT 'ai:chat', '对话', 'AI', '使用对话接口', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'ai:chat');
INSERT INTO sys_permission (code, name, group_name, description, created_at, updated_at)
SELECT 'ai:knowledge:read', '知识库查看', 'AI', '查看文档与检索结果', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'ai:knowledge:read');
INSERT INTO sys_permission (code, name, group_name, description, created_at, updated_at)
SELECT 'ai:knowledge:write', '知识库上传', 'AI', '上传资料（仅管理员）', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'ai:knowledge:write');
INSERT INTO sys_permission (code, name, group_name, description, created_at, updated_at)
SELECT 'ai:knowledge:admin', '知识库管理', 'AI', '重建索引/删除文档（仅管理员）', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'ai:knowledge:admin');
INSERT INTO sys_permission (code, name, group_name, description, created_at, updated_at)
SELECT 'sys:user:manage', '用户管理', '系统', '用户 CRUD 与禁用', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'sys:user:manage');
INSERT INTO sys_permission (code, name, group_name, description, created_at, updated_at)
SELECT 'sys:role:manage', '角色管理', '系统', '角色与权限分配', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'sys:role:manage');
INSERT INTO sys_permission (code, name, group_name, description, created_at, updated_at)
SELECT 'sys:menu:manage', '菜单管理', '系统', '菜单配置', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'sys:menu:manage');

-- 平台应用
INSERT INTO sys_app (code, name, tagline, description, expert_id, status, sort_order, created_at, updated_at)
SELECT 'health', '健康科普顾问', '示例应用',
       '居家营养与健康科普场景：本地知识库问答、引用溯源与资料不足拒答。用于练习 RAG、会话与评测链路。',
       'home-health-advisor', 1, 1, NOW(6), NOW(6)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_app WHERE code = 'health');

-- 菜单（平台导航：先根后子；对话入口在示例应用内 /apps/health/chat）
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

-- 用户-角色
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON r.code = 'ADMIN' WHERE u.username = 'admin';

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON r.code = 'USER' WHERE u.username = 'user';

-- 角色-权限：管理员全部；普通用户 chat + knowledge:read
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r CROSS JOIN sys_permission p WHERE r.code = 'ADMIN';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.code IN ('ai:chat', 'ai:knowledge:read')
WHERE r.code = 'USER';

-- 角色-菜单：管理员全部；普通用户 chat + knowledge
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id FROM sys_role r CROSS JOIN sys_menu m WHERE r.code = 'ADMIN';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.path IN ('/workspace', '/apps', '/knowledge')
WHERE r.code = 'USER';
