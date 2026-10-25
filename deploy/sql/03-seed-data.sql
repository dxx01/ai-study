-- 演示账号（幂等；密码见 DemoPasswords / 文档）
USE ai_platform;

INSERT INTO sys_user (username, password_hash, roles, permissions, created_at)
SELECT 'admin', '$2a$10$hYblB87eO6gr8Hx3n334I.AJvbpFdjHh0E/G9ivY/mZAwkpi5Sbg2',
       JSON_ARRAY('admin'),
       JSON_ARRAY('ai:chat', 'ai:knowledge:read', 'ai:knowledge:write', 'ai:knowledge:admin'),
       CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

INSERT INTO sys_user (username, password_hash, roles, permissions, created_at)
SELECT 'user', '$2a$10$ES3P14obVDPbAQkpPz2b/OhNflNB4cCz6SAaV95Q2vvYzUoqtUomy',
       JSON_ARRAY('user'),
       JSON_ARRAY('ai:chat', 'ai:knowledge:read'),
       CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'user');
