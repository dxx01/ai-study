# 学习日志

> 每个 Phase 记一节：做了什么、数字、踩坑、取舍、下一步。
> 目的不是流水账，而是留下“为什么这样改”和“改完好了多少”的证据。

## Phase 0 — 基线可运行（2026-10-25）

### 做了什么

- 统一“手工 DDL”为唯一事实源，清理 Flyway 相关文档。
- Docker 空数据卷首次启动自动建库、建表、写入演示账号。
- 修复 3 个运行时缺陷（登录权限、`@PathVariable`、网关 401）。
- 排除知识库/LLM 无用的 DataSource 自动装配，增加本地模型 profile。
- 补 Spring Boot `repackage`，产出可执行 jar。
- 本地数据库密码移入 gitignored 的 `deploy/env/local-mysql.env`。
- 增加项目说明书，明确范围、非目标与验收指标。

### 数字

| 项 | 结果 |
|----|------|
| 后端编译 | 8 个 reactor 模块通过 |
| 可执行 jar | auth 40.7 MB / gateway 42.9 MB / knowledge 200.7 MB |
| 登录 | 200，返回 4 个权限 |
| 会话 | 创建 + 追加 + 查询通过，1 条消息 |
| 网关 | 未登录 401；带 token 路由成功 |
| 单测 | `UserStoreServiceTest` 1/1 通过 |
| 对话端到端 | 上传 1 篇 → 命中 1 条本地引用 → 2.6s 返回正确答案 |
| 前端构建 | Node 24.10.0，`vue-tsc -b && vite build` 通过（1694 modules，5.8s） |

### 踩坑（按重要性）

1. **JSON 列权限为空**：MyBatis-Plus 自定义 `@Select` 不走 `autoResultMap`，`JacksonTypeHandler` 不生效，登录返回 `permissions: []`，前端菜单/路由直接失效。  
   教训：JSON 列查询必须走 BaseMapper（或显式 resultMap），并用“重新读库”的断言防回归。
2. **`@PathVariable` 报 400**：项目没有 `spring-boot-starter-parent`，编译器默认不带 `-parameters`，Spring 无法反射参数名。  
   教训：不用 parent 时，编译插件要显式 `<parameters>true</parameters>`。
3. **未登录返回 500**：Sa-Token 默认错误策略抛异常，被 Spring 兜底。  
   教训：网关要自己写 401/403，前端才能正确跳登录。
4. **`java -jar` 没有主清单**：同样因为不使用 parent，`repackage` 不会自动绑定。  
   教训：`spring-boot-maven-plugin` 要显式声明 `repackage` execution。
5. **知识库启动卡在模型下载**：默认 ONNX 模型从 GitHub 拉取，国内网络极易卡住。  
   教训：涉及外部模型/权重的服务，必须提供缓存目录、离线文件和环境变量覆盖；已增加 `local-model` profile。
6. **纯 Redis 服务却要 DataSource**：`ai-common` 传递了 MyBatis-Plus/JDBC，knowledge/llm 启动时报 `Failed to configure a DataSource`。  
   教训：公共模块要区分“编译依赖”和“运行依赖”；当前用 `spring.autoconfigure.exclude` 排除 `DataSourceAutoConfiguration`，后续可拆公共模块。

### 取舍

- 手工 DDL 而不是 Flyway：单人、结构简单、便于审计；代价是改表要同步脚本/实体/文档。
- `SimpleVectorStore` 而不是 PGVector：先跑通链路；代价是内存态、重启重建，Phase 3 必须替换。
- 单体前端而不是 SSR：演示够用，先把 AI 链路做对。

### 下一步

Phase 1：20–30 条金标问题 + 指标脚本（recall@5、引用命中、拒答率、延迟），先拿到基线，再谈 RAG 调优。
