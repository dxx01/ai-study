# 数据库设计规范

适用于本仓库 **MySQL 8** + **MyBatis-Plus**。前期**不使用 Flyway**；DDL 以 `deploy/sql/02-schema.sql` 与 [schema.md](../database/schema.md) 为事实来源，改表时同步脚本、实体与文档。

## 1. 命名

| 对象 | 规则 | 示例 |
|------|------|------|
| 库名 | 小写 snake | `ai_platform` |
| 表名 | 小写 snake，带业务前缀 | `sys_user`, `chat_session` |
| 列名 | 小写 snake | `owner_user_id`, `created_at` |
| 主键 | 单列 `id`（或业务明确 UUID） | |
| 唯一索引 | `uk_表名_列名` | `uk_sys_user_username` |
| 普通索引 | `idx_表名_列名` | `idx_chat_message_session_created_id` |
| 外键 | `fk_从表_主表` | `fk_chat_message_session` |

Java 属性 camelCase，列名用 `@TableField("snake_case")` 显式映射。

## 2. 注释（强制）

- 每张表：`COMMENT='业务用途'`  
- 每个业务列：`COMMENT '含义；必要时写取值/单位/规则'`  

## 3. 类型与约束

- 主键关联字段 **类型一致**（`sys_user.id` BIGINT ↔ `chat_session.owner_user_id` BIGINT）  
- 时间：`DATETIME(6)`，时区 **Asia/Shanghai**；Java 用 `LocalDateTime`（与 JDBC `serverTimezone=Asia/Shanghai` 一致）  
- JSON：roles/permissions 等简单集合  
- 长文本：消息体 `MEDIUMTEXT`，API 限制 16KB  

## 4. 微服务与 FK

- **跨服务**：逻辑关联 ID，**不建 FK**（session → user）  
- **同服务同库**：可建 FK（message → session CASCADE）  

## 5. 索引

- 每个索引对应具体查询与 `ORDER BY`  
- 消息列表：`session_id, created_at, id`  

## 6. 时间字段

- 统一 `created_at`；需要时 `updated_at`  
- **由应用赋值**（MyBatis-Plus `AuditMetaObjectHandler`，上海时区）  
- `updated_at` 在 update 时由同一 Handler 维护  

## 7. 变更管理（前期手工 DDL）

1. 修改 `deploy/sql/02-schema.sql`（本地/部署初始化）  
2. 已存在环境：编写 **增量 SQL**（注释说明目的），在 PR 中说明执行顺序  
3. 同步 MyBatis-Plus 实体与 [schema.md](../database/schema.md)  
4. **禁止**仅改实体或仅改库而不更新脚本/文档  

稳定期若引入 Flyway/Liquibase，再单独评审。

## 8. deploy/sql 目录

| 文件 | 用途 |
|------|------|
| `01-init-database.sql` | 建库 |
| `01b-grant-docker-user.sql` | Docker 用户授权 |
| `02-schema.sql` | 业务表 DDL |
| `03-seed-data.sql` | 演示数据（幂等） |

本地：`scripts/init-mysql-local.ps1` 依次执行 01 → 02 → 03。

## 9. 检查

交付前对照 [database-review-checklist.md](database-review-checklist.md)。
