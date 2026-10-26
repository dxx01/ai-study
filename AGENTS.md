# AI 应用平台 — Agent 协作约定

## 先读

- [docs/project-charter.md](docs/project-charter.md)：项目定位、范围、非目标、验收指标；新需求先过 Scope Guard。

## 协作模式（人与 Agent）

> `AGENTS.md` 是 DSH 与 Cursor 的共用入口；`.cursor/rules/*.mdc` 是 Cursor 适配层，冲突时以本文件为准。

### 方案 / 建议模式（默认不写代码）

**触发条件**：本轮主诉求是“要方案 / 要建议 / 讨论设计”，且包含任一触发词：

`讨论`、`建议`、`方案`、`设计`、`选型`、`评估`、`是否合理`、`有什么建议`、`给点建议`、`你怎么看`。

不触发：纯事实问答、报错修复、明确说“实现 / 开发 / 按方案改代码”。

**进入后必须：**

1. 先读 [通用审查清单](docs/standards/design-review.md)、[本仓库补强](docs/standards/design-review-ai-study.md) 和 [project-charter.md](docs/project-charter.md)。
2. 用 `read` / `grep` 核对相关代码、配置、库表；区分**事实**与**假设**，未核实的写“待核实”。
3. 默认**不写代码、不改配置、不提交**；只有用户明确说“实现 / 开发 / 按方案改代码”才退出本模式。
4. 中大型方案（库表、跨服务、新实体）输出：**现状 → 推荐 → 备选 → 审查摘要 → 本期必须 / 建议预留 / 暂不实现 → 待确认与验收**；简单问题可压缩为“结论 + 少量风险”。
5. 不扩大范围；不把“未来可能”写成“本期必须”。

### 实现 / 改代码

用户说「按上面方案 M1 实现」「直接改 bug」时，按最小 diff 实施，遵守 Java、前端、库表与权限规范。

### Review（代码）

用户消息去掉首尾空白后以 `RV` / `rv` 或 `RV:代码` / `rv:代码` 开头时：

1. **必读** [docs/standards/code-review.md](docs/standards/code-review.md)（仓库级标准，Cursor / DSH 共用）。
2. 按该文档 §2 收集证据（优先未提交 `git diff`；构建/测试、关键路径运行证据），再按 §3–§5 审查并给出结论。
3. **默认不改代码、不 commit**；用户明确「RV 并修 / 按 RV 改」时，仅针对 P0/P1 最小修改。
4. Cursor 若加载个人 Skill `review-playbook`：**检查项、结论与输出格式均以 `code-review.md` 为准**（§6）；Skill 仅作触发辅助。

`RV:设计` / `RV:需求` 及未列入上文的 `RV:…` **不触发**代码 review；方案层面用上文**方案模式** + [design-review.md](docs/standards/design-review.md)。

## 微服务

| 模块 | 端口 |
|------|------|
| `backend/ai-gateway` | 9100 |
| `backend/ai-auth` | 9101 |
| `backend/ai-service-llm` | 9201 |
| `backend/ai-service-knowledge` | 9202 |
| `backend/ai-service-conversation` | 9203 |
| `frontend` | 5173 |

禁止 `*-demo` 命名。跨服务 DTO 在 `backend/ai-common` → `com.example.ai.common.api.dto`。

## 强制流程

1. **Java**：[java-enterprise-coding-standards.mdc](.cursor/rules/java-enterprise-coding-standards.mdc)  
2. **前端**：[frontend-standards.mdc](.cursor/rules/frontend-standards.mdc)  
3. **数据库 / 数据访问 / 安全**：必须先读 
 - [docs/standards/database-design.md](docs/standards/database-design.md) 
 - [docs/standards/dictionaries.md](docs/standards/dictionaries.md)（状态/字典字段） 
   - [docs/standards/data-access.md](docs/standards/data-access.md)  
   - [docs/standards/data-security.md](docs/standards/data-security.md)  
   交付前对照 [docs/standards/database-review-checklist.md](docs/standards/database-review-checklist.md)  
4. **库表文档**：逻辑结构 [docs/database/schema.md](docs/database/schema.md)；本地与部署分开见 [docs/database/README.md](docs/database/README.md)  
5. **结构变更**：同步 `deploy/sql/02-schema.sql` + MyBatis-Plus 实体 + schema.md（前期不用 Flyway）  
6. **构建**：JDK 17 → `.\scripts\use-jdk17.ps1`  

## 表命名与权限约定（强制）

- 系统表统一 `sys_` 前缀（`sys_user`、`sys_role`、`sys_permission`、`sys_menu`、关联表 `sys_user_role` 等）。
- 业务表按领域/服务前缀：`chat_*`、`knowledge_*`；禁止 `t_`/`tb_`/`*_demo`。
- 关联表用 `<主表>_<从表>`，如 `sys_role_permission`。
- 权限模型见 [docs/standards/access-control.md](docs/standards/access-control.md)；上传、删除、重建索引等敏感操作必须后端 `@SaCheckPermission` 校验，不能只靠前端隐藏按钮。

## Git 与交付流程（强制）

- **默认只改工作区：不 commit、不 push。** 改完先报告改动文件、验证方式与 diff，等用户验证或 review。
- **commit 需要用户明确要求**（例如“提交”“整理成 commit”）。提交前先 `git status` + `git diff` 自查，只包含本次任务相关文件。
- **push 需要用户再次明确要求**（例如“push”“推送”“上传 GitHub”）。push 前确认当前分支、远端和 ahead 提交；优先 feature 分支，**不直接推 main**。
- commit 与 push 是两步：用户同意 commit 不代表同意 push；用户同意 push 也不代表可以顺带提交未确认的改动。
- 禁止 `git push --force`、改写已推送历史、修改他人提交。
- 禁止提交 `.env*`、API Key、数据库密码、`target/`、`node_modules/`、本地数据和评测报告。
- 一个 commit 只做一件事；提交信息写清“做了什么 / 为什么”。
- 交付说明必须包含：改了哪些文件、怎么验证的、**当前是否已 commit、是否已 push**。

## 启动

见 [README.md](README.md)、[docs/microservices.md](docs/microservices.md)。
