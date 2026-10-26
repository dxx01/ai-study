# 数据库变更审查清单

涉及 **DDL 脚本 / MyBatis-Plus 实体 / Mapper / schema 文档** 时使用。

- [ ] 表名、字段名、索引名符合 [database-design.md](database-design.md)
- [ ] 系统表 `sys_` 前缀，业务表按领域前缀（`chat_`/`knowledge_`），关联表用 `<主表>_<从表>`
- [ ] 角色/权限/菜单变更同步 `sys_*` 表、`03-seed-data.sql` 与 [access-control.md](access-control.md)
- [ ] 表与业务字段均有准确 COMMENT（`02-schema.sql` 内）
- [ ] 字段类型、长度、可空、默认值有业务依据
- [ ] 状态/类型等字典字段为 `TINYINT` 数字码，COMMENT 与 [dictionaries.md](dictionaries.md)、前后端字典一致
- [ ] 主键与关联字段类型一致（如 `owner_user_id` BIGINT）
- [ ] 索引匹配真实查询与 `ORDER BY`（含稳定次级键）
- [ ] 无不必要的全表查询/大范围更新
- [ ] 时间字段 Asia/Shanghai + `LocalDateTime`，自动填充与业务更新责任明确
- [ ] SQL 参数化；用户数据带归属/权限过滤
- [ ] `02-schema.sql`、增量 SQL、兼容性说明；破坏性变更有备份方案
- [ ] 实体、`02-schema.sql`、[schema.md](../database/schema.md) 一致
- [ ] 必要测试已执行或说明未测项
