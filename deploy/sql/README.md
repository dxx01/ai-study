# 部署用 SQL

| 文件 | 用途 |
|------|------|
| `01-init-database.sql` | 创建 `ai_platform` 库 |
| `01b-grant-docker-user.sql` | Docker 应用用户 `ai` |
| `02-schema.sql` | 业务表 DDL（与 MyBatis-Plus 实体、schema.md 一致） |
| `03-seed-data.sql` | 演示账号（幂等） |

Docker Compose 通常挂载 `01` + `01b`；完整建表见 [docs/database/deployment.md](../../docs/database/deployment.md)。

本地：`scripts/init-mysql-local.ps1`（可选 `-Recreate`）执行 01 → 02 → 03。
