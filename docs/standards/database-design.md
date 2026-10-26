# 数据库设计规范

适用于本仓库 **MySQL 8** + **MyBatis-Plus**。前期**不使用 Flyway**；DDL 以 `deploy/sql/02-schema.sql` 与 [schema.md](../database/schema.md) 为事实来源，改表时同步脚本、实体与文档。

## 1. 命名

| 对象 | 规则 | 示例 |
|------|------|------|
| 库名 | 小写 snake | `ai_platform` |
| 系统表 | **`sys_` 前缀** | `sys_user`, `sys_role`, `sys_permission`, `sys_menu` |
| 业务表 | 按**业务/服务前缀** | `chat_session`, `knowledge_document` |
| 关联表 | `<主表>_<从表>`，系统关联仍带 `sys_` | `sys_user_role`, `sys_role_permission` |
| 列名 | 小写 snake | `owner_user_id`, `created_at` |
| 主键 | 单列 `id`（或业务明确 UUID） | |
| 唯一索引 | `uk_表名_列名` | `uk_sys_user_username` |
| 普通索引 | `idx_表名_列名` | `idx_chat_message_session_created_id` |
| 外键 | `fk_从表_主表` | `fk_chat_message_session` |

**前缀约定（强制）：**

- 用户、角色、权限、菜单、字典等**系统级表**统一 `sys_` 开头。
- 业务表按领域/服务命名：对话 `chat_*`、知识库 `knowledge_*`；以后新增领域用 `order_*`、`billing_*` 等。
- 禁止 `t_`、`tb_`、`tbl_` 前缀，禁止 `*_demo` 命名。
- 关联表用两张表名拼接，例如 `sys_user_role`、`sys_role_permission`。

Java 属性 camelCase，列名用 `@TableField("snake_case")` 显式映射。

权限表设计（用户/角色/权限/菜单）见 [access-control.md](access-control.md)。

## 2. 注释（强制）

- 每张表：`COMMENT='业务用途'`  
- 每个业务列：`COMMENT '含义；必要时写取值/单位/规则'`  

## 3. 类型与约束

- 主键关联字段 **类型一致**（`sys_user.id` BIGINT ↔ `chat_session.owner_user_id` BIGINT）  
- 时间：`DATETIME(6)`，时区 **Asia/Shanghai**；Java 用 `LocalDateTime`（与 JDBC `serverTimezone=Asia/Shanghai` 一致）  
- JSON：roles/permissions 等简单集合  
- 长文本：消息体 `MEDIUMTEXT`，API 限制 16KB  
- **字典/状态**：优先 `TINYINT`，取值 `0、1、2…`；列 COMMENT 写清含义；禁止 `ACTIVE`/`DISABLED` 等英文字符串落库。详见 [dictionaries.md](dictionaries.md)  

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
| `02-schema.sql` | v0.7 全量 DDL（新库） |
| `03-seed-data.sql` | 演示数据 + 角色/权限/菜单（幂等） |
| `04-schema-v2.sql` | v0.6 增量（已有库，只执行一次） |
| `05-rbac.sql` | v0.7 系统权限表（已有库，只执行一次） |
| `06-fix-comments.sql` | 修复旧表/旧列中文注释乱码（不改数据，只执行一次） |
| `07-platform-nav.sql` | 侧栏迁移为「AI 应用平台」导航（已有库可执行，幂等） |
| `08-enable-status-tinyint.sql` | 用户/角色/菜单 `status` 改为 0/1（已有库执行一次） |
| `09-sys-app-and-knowledge-app.sql` | `sys_app` + 知识库 `app_id`/`category`（已有库执行一次） |

本地：新库 `scripts/init-mysql-local.ps1` 执行 01 → 02 → 03；已有库执行 `04`～`09` 后，再执行一次 `03`。
执行 SQL 时使用 `--default-character-set=utf8mb4`，避免中文注释乱码。

## 9. 检查

交付前对照 [database-review-checklist.md](database-review-checklist.md)。
