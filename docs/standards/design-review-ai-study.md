# 方案审查 — ai-study 项目补强

> 在 [design-review.md](design-review.md) 通用清单之后阅读。仅适用于本仓库。

## 范围与非目标

- [project-charter.md](../project-charter.md)：v0.5 范围、非目标（非诊疗、非多租户 SaaS 等）。

## 架构与模块

- 微服务边界：网关鉴权、LLM 编排、knowledge RAG、conversation 持久化（见 [architecture-brief.md](../architecture-brief.md)）。
- 跨服务 DTO：`backend/ai-common`；禁止 `*-demo` 命名。

## 数据与库表

- 逻辑模型：[database/schema.md](../database/schema.md)；DDL：`deploy/sql/02-schema.sql`。
- 系统表 `sys_` 前缀；业务 `knowledge_*`、`chat_*`。
- 状态字段：`docs/standards/dictionaries.md`（TINYINT + 前后端字典）。
- 变更同步：SQL + 实体 + schema.md；对照 [database-review-checklist.md](database-review-checklist.md)。

## 安全与配置

- 网关统一登录；业务 `@SaCheckPermission`（[access-control.md](access-control.md)）。
- 连接：`deploy/env/local-mysql.env`、`deploy/env/server.app.env`；密钥不进 Nacos 明文。
- LLM Key：`backend/ai-service-llm/.env.dev.properties`（`DEEPSEEK_API_KEY` / `OPENAI_API_KEY` 兼容）。

## 平台演进常见漏项

- 引入 `app_id` / 多应用时：是否需 **`sys_app`**，与 `demoApps.ts`、`expert`、知识库检索过滤是否一致。
- 知识库：应用 vs 资料分类（category）；向量 metadata 过滤；对话 search 是否带 appId。
- Nacos：路由 `lb://`；配置与 jar 内 `application.yml` 分工。

## 实现阶段（退出方案模式后）

- Java：[java-enterprise-coding-standards.mdc](../../.cursor/rules/java-enterprise-coding-standards.mdc)
- 前端：[frontend-standards.mdc](../../.cursor/rules/frontend-standards.mdc)
- 合并前代码审查（`RV` / `RV:代码` 前缀，含 DoD/验收矩阵）：[code-review.md](code-review.md)
