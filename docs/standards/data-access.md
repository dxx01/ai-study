# 数据访问规范

持久层：**MyBatis-Plus**（`BaseMapper` + 注解/SQL）。前期**不使用 Flyway**；表结构以 `deploy/sql/02-schema.sql` 与 [schema.md](../database/schema.md) 为准，变更时同步脚本与文档。

## 1. 分层

- Controller：参数校验、鉴权，不写 SQL  
- Service：事务、业务编排、**权限过滤**  
- Mapper：数据访问；自定义 SQL 使用 `@Select`/`@Param`，禁止字符串拼接  

## 2. 实体映射

- 实体 `@TableName` / `@TableField` 与 DDL 一致  
- 时间字段：`LocalDateTime` ↔ `DATETIME(6)`，时区 **Asia/Shanghai**（JDBC `serverTimezone=Asia/Shanghai`）  
- JSON 列（如 `roles`）：`JacksonTypeHandler` + `autoResultMap = true`  
- 审计字段使用 `@TableField(fill = FieldFill.INSERT|INSERT_UPDATE)`，由 `AuditMetaObjectHandler` 填充  

## 3. 登录用户上下文与自动填充

- `LoginUserContextInterceptor`（order=10，在 Sa-Token 之后）将 `StpUtil.getLoginIdAsLong()` 写入 `LoginUserContext`  
- `AuditMetaObjectHandler` 在 **insert** 时填充：`createdAt`、`updatedAt`（上海时区）；若字段存在且为空则填充 `ownerUserId`、`createdBy`  
- **update** 时填充 `updatedAt`  
- 业务仍须在 Service 层校验 **归属**（如 `owner_user_id`），不得仅依赖自动填充  

## 4. 查询与排序

- 用户数据必须带 **归属条件**（如 `selectByIdAndOwner`）  
- 不得仅信任前端传入的 userId/sessionId  
- 消息列表：**稳定排序** `created_at, id`（倒序取 N 条后业务反转为正序）  

## 5. 事务

- 写操作 `@Transactional`；读 `readOnly = true`  
- append 消息 + 更新 session 同一事务  

## 6. 分页与上限

- 列表接口限制上限（conversation `MAX_LIMIT=100`）  
- 深分页评估 keyset/cursor  

## 7. 测试

- H2 + `schema-test.sql` + MyBatis-Plus，或 Testcontainers MySQL  
- 必测：权限隔离（越权 session）、空结果、边界长度  

## 8. DTO 边界

- API 使用 `ai-common` 的 DTO/record；实体不直接暴露  
