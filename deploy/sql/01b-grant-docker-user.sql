-- 应用账号 ai / ai_dev_password（Docker Compose 与本地初始化共用）
CREATE USER IF NOT EXISTS 'ai'@'%' IDENTIFIED BY 'ai_dev_password';
CREATE USER IF NOT EXISTS 'ai'@'localhost' IDENTIFIED BY 'ai_dev_password';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
  ON ai_platform.* TO 'ai'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
  ON ai_platform.* TO 'ai'@'localhost';

FLUSH PRIVILEGES;
