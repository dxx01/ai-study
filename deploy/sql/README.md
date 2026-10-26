# 部署用 SQL

| 文件 | 用途 |
|------|------|
| `01-init-database.sql` | 创建 `ai_platform` 库 |
| `01b-grant-docker-user.sql` | Docker 应用用户 `ai` |
| `02-schema.sql` | v0.7 全量 DDL（新库直接执行） |
| `03-seed-data.sql` | 演示账号 + 角色/权限/菜单（幂等） |
| `04-schema-v2.sql` | 已有库升级到 v0.6（**只执行一次**） |
| `05-rbac.sql` | 已有库增加系统权限表（**只执行一次**） |
| `06-fix-comments.sql` | 修复旧表/旧列中文注释乱码（**只执行一次，不改数据**） |
| `07-platform-nav.sql` | 侧栏迁移为「AI 应用平台」导航（已有库可执行，幂等） |
| `08-enable-status-tinyint.sql` | 用户/角色/菜单 `status` 改为 0/1（已有库执行一次） |

Docker Compose 通常挂载 `01` + `01b`；完整建表见 [docs/database/deployment.md](../../docs/database/deployment.md)。

本地：新库用 `scripts/init-mysql-local.ps1` 执行 01 → 02 → 03；已有库依次执行 `04-schema-v2.sql`、`05-rbac.sql`、`06-fix-comments.sql`、`07-platform-nav.sql`、`08-enable-status-tinyint.sql`，再执行一次 `03-seed-data.sql`。

> 所有 SQL 执行都要带 `--default-character-set=utf8mb4`，否则中文注释可能乱码。

