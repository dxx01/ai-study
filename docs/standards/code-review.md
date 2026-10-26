# 代码审查清单（RV）

> **用途**：用户消息以 `RV` / `RV:代码` 开头时，按本文审查**已实现**的代码、配置与部署文件。  
> **不替代** [design-review.md](design-review.md)（方案/范围）。设计通过后，实现与合并前走本文。  
> **触发与行为**：见 [AGENTS.md](../../AGENTS.md) §Review（代码）。

## 1. 审查范围

| 包含 | 不包含 |
|------|--------|
| `backend/**`、`frontend/**` | 纯产品文案、会议记录（除非 @ 且与 diff 相关） |
| `deploy/**`（sql、nacos、compose、env 示例） | 用 design-review 讨论的「要不要做」 |
| `.cursor/rules/**`、与运行相关的 `scripts/**` | 单纯风格偏好 |

**材料优先级**：用户 @ 的文件 → `RV` 后说明 → **未提交 `git diff`** → 无则请用户指定范围。

**范围是否做完**：若用户声称某需求已交付，对照 [project-charter.md](../project-charter.md) 用户旅程或设计文档「本期必须」；RV 负责给证据和结论，最终产品签收仍由用户确认。

## 2. 审查前必须拿到的证据

没有证据的“看起来没问题”不能作为通过依据。

- [ ] **改动范围**：`git status` / `git diff` 已看，确认只含本次任务
- [ ] **构建/测试**：已运行相关 `mvn test`、`npm run build` 或定向测试，并记录结果
- [ ] **关键路径**：登录、权限、数据写入、配置加载至少有一条运行证据（接口响应 / DB 行 / 日志）
- [ ] **库表变更**：DDL / 增量 SQL 已执行或说明未执行；实体、schema、`02-schema.sql` 一致
- [ ] **未验证项**：未运行、未覆盖、无法复现的内容必须显式列出

> 关键路径没有任何运行证据时，RV 不能给“通过”，最多给“有条件通过/不通过”。

**Agent 执行**：应在本机尝试运行与 diff 相关的 `mvn test` / `npm run build` 或定向命令；若环境不可用（无 MySQL、Nacos 未连等），不得给「通过」，须标 **未跑命令 + 建议你本地执行的步骤**（见 §6「未覆盖」）。

## 3. 检查项（按 diff 类型选用）

### 3.1 通用（每次 RV）

- [ ] **diff 范围**：是否只含本次任务；是否混入 `.env`、密钥、`target/`、`node_modules/`、无关 IDE 文件
- [ ] **正确性**：主路径、边界（空/null/非法参数）、错误是否可理解（非裸 500）
- [ ] **契约**：前后端字段、API 路径、DTO 是否与调用方一致（跨服务 DTO 在 `ai-common`）
- [ ] **权限**：敏感接口是否有 `@SaCheckPermission`；是否仅靠前端 `v-if` 隐藏
- [ ] **测试**：是否新增/更新了对应测试；没有测试的依据是什么
- [ ] **规范**：Java [java-enterprise-coding-standards.mdc](../../.cursor/rules/java-enterprise-coding-standards.mdc)、前端 [frontend-standards.mdc](../../.cursor/rules/frontend-standards.mdc)

### 3.2 后端 / API

- [ ] 事务与一致性：多表/跨步失败是否回滚或明确补偿
- [ ] 幂等与并发：重复提交、并发计数、重复消费是否有保护
- [ ] 归属与隔离：如 `app_id`、`owner_user_id` 是否在后端校验，防越权
- [ ] 外部调用：超时、失败降级、不泄漏上游密钥
- [ ] 日志与错误：不输出密钥/密码；异常不吞掉关键上下文

### 3.3 前端

- [ ] 请求 loading / 错误反馈（勿静默失败）
- [ ] 路由、权限与菜单一致；破坏性路由是否兼容旧链接
- [ ] 类型与 API 响应字段对齐（`types/api.ts`）
- [ ] 富文本/Markdown 是否做 XSS 清洗；权限按钮是否同时有后端校验

### 3.4 配置与部署

- [ ] `dev` / `server` / Nacos 分工：新路由或配置是否需 **发布 Nacos**（如网关）
- [ ] 无硬编码密钥；`deploy/env` 示例不含真实密码；密钥不出现在日志、异常栈、前端响应
- [ ] 新增路由、端口、安全组或公开访问是否有风险说明
- [ ] 环境变量名与 `deploy/env/local.env`、`server.env`、`current.env` 及文档一致（见 [microservices.md](../microservices.md)）
- [ ] **local / server 均注册并拉取 Nacos**（`application-common.yaml` + 服务 yaml）；改 `deploy/nacos/configs` 后是否需 `publish-nacos-configs` / 发布说明

### 3.5 库表（diff 含 DDL / 实体时）

- [ ] 完整走 [database-review-checklist.md](database-review-checklist.md)
- [ ] `02-schema.sql`、增量 SQL、`schema.md`、实体字段一致
- [ ] 字符集/排序规则、时间语义、索引、状态字段字典一致

## 4. 严重程度

| 级别 | 定义 | 例子 |
|------|------|------|
| **P0** | 不能交付，必须修 | 越权、数据丢失、密钥泄漏、启动/迁移失败、关键路径无运行证据 |
| **P1** | 应修；可限时接受 | 验收项缺失、关键异常路径未处理、无测试且无说明、文档未同步 |
| **P2** | 可选 | 命名、风格、非关键可维护性 |

> “缺测试说明”单独出现是 P1；如果缺的是登录/权限/数据写入等关键路径，升级为 P0。

## 5. 结论与 Definition of Done

### 5.1 结论

- **通过**：P0 = 0，关键路径有证据，P1 = 0
- **有条件通过**：P0 = 0，但存在明确接受理由和截止时间的 P1
- **不通过**：存在 P0，或关键验收项没有证据

### 5.2 Definition of Done（功能是否做完）

- [ ] 本次验收标准逐条有“通过 / 不通过 / 未验证”
- [ ] 主流程可复现：命令、请求、预期结果
- [ ] 异常与权限至少验证一次（未登录、无权限、非法输入）
- [ ] 数据落库或迁移有证据
- [ ] `mvn test` / `npm run build` 或定向测试有结果
- [ ] `dev` / `server` / Nacos 配置没有倒退
- [ ] 文档、schema、README/roadmap 与实现一致
- [ ] 未验证项和已知风险已列出

RV 只对“证据覆盖到的范围”负责；没有证据的功能不能算完成。

## 6. 输出格式

```markdown
## RV：代码审查

**范围**：（未提交 diff / 指定路径 …）

**代码结论**：通过 / 有条件通过 / 不通过
**功能 DoD**：完成 / 未完成 / 部分完成

### 验收矩阵
| 验收项 | 证据 | 结果 |

### P0 必须处理
（无则写「无」）

### P1 建议处理

### P2 可选

### 未覆盖 / 待你确认
（未读文件、未跑命令、需要你本地验证的项）
```

默认不改代码；用户说「RV 并修」「按 RV 改」时，仅针对 P0/P1 做最小 diff。

输出格式以 **本文 §6** 为准；个人 Skill `review-playbook` 若不一致，以本文为准。

## 7. 与其它文档的关系

| 阶段 | 文档 |
|------|------|
| 讨论 / 建议 / 方案 | [design-review.md](design-review.md) + [design-review-ai-study.md](design-review-ai-study.md) |
| 代码 / 配置 Review（`RV`） | **本文** |
| 库表专项 | [database-review-checklist.md](database-review-checklist.md) |
| 安全专项 | [data-security.md](data-security.md)、[access-control.md](access-control.md) |

`RV:设计` / `RV:需求` 不触发本文，按方案模式处理。
