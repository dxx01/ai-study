-- Docker 首次初始化：建库；表结构与种子由同目录 02-schema.sql / 03-seed-data.sql 执行
-- 仅在数据卷为空时由 MySQL entrypoint 自动执行；存量卷请手工执行 02/03 或运行 scripts/init-mysql-local.ps1
CREATE DATABASE IF NOT EXISTS ai_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
