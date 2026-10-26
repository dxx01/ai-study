# 微服务运行说明

## 服务与端口

| 服务 | 模块 | 端口 |
|------|------|------|
| 网关 | `backend/ai-gateway` | 9100 |
| 认证 | `backend/ai-auth` | 9101 |
| LLM | `backend/ai-service-llm` | 9201 |
| 知识库 | `backend/ai-service-knowledge` | 9202 |
| 会话 | `backend/ai-service-conversation` | 9203 |

## 数据库

- **逻辑模型**：[database/schema.md](database/schema.md)  
- **本地 MySQL**：[database/local-development.md](database/local-development.md)  
- **Docker 部署**：[database/deployment.md](database/deployment.md)  

表结构以 `deploy/sql/02-schema.sql` 为准（前期手工 DDL，不用 Flyway）。Docker 首次启动（空数据卷）会自动执行 `01 → 01b → 02 → 03`；已有数据卷或本地 MySQL 用 `scripts/init-mysql-local.ps1` 初始化。

连接环境：维护 **`deploy/env/local.env`** 或 **`server.env`**（仅 MySQL/Redis 不同），执行 **`.\scripts\use-env.ps1 -Target local|server`** 生成 `current.env`；各服务统一读 `current.env` 并**始终注册 Nacos、拉取** `application-common.yaml` 与 `<服务名>.yaml`。未放行公网 Nacos 时先 **`.\scripts\start-nacos-tunnel.ps1`**。

## Nacos

- 各服务启动即注册 Nacos，并拉取 `application-common.yaml` + `<服务名>.yaml`；网关路由见 `ai-gateway.yaml`（`lb://`）。
- 配置内容见 `deploy/nacos/configs/`，发布脚本 `deploy/nacos/publish-configs.sh`；说明见 [deploy/nacos/README.md](../deploy/nacos/README.md)。
- 本地通过 `.\scripts\start-nacos-tunnel.ps1` 建立 8848/9848 隧道（安全组未放行时）。
- Nacos 不可达时各服务 `optional:nacos:` 可能降级启动，但网关无 `lb://` 路由；请先 `check-nacos.ps1` 与隧道。

## 启动顺序

```powershell
.\scripts\use-jdk17.ps1
# use-env.ps1 -Target local；Nacos 隧道见 start-nacos-tunnel.ps1；MySQL 初始化见 init-mysql-local.ps1

mvn -q -DskipTests install   # 首次；-pl 单模块启动依赖本地仓库中的 ai-common

mvn -pl backend/ai-auth spring-boot:run
mvn -pl backend/ai-service-knowledge spring-boot:run
mvn -pl backend/ai-service-conversation spring-boot:run
mvn -pl backend/ai-service-llm spring-boot:run
mvn -pl backend/ai-gateway spring-boot:run
```

前端：`cd frontend && npm install && npm run dev`（Node 18+）→ http://localhost:5173

## 知识库与 embedding 模型

- 知识库默认用 `spring-ai-starter-model-transformers`（ONNX）。**首次启动**会从 GitHub 下载 `all-MiniLM-L6-v2`（约 90MB），缓存到 `backend/ai-service-knowledge/data/model-cache`（可用 `EMBEDDING_CACHE_DIR` 覆盖）。
- 网络受限时（国内访问 GitHub raw 常见）会卡在 `Caching the URL ...`。可预下载 `model.onnx` / `tokenizer.json` 到
  `backend/ai-service-knowledge/data/model-cache/all-MiniLM-L6-v2/`，然后生成配置：

  ```powershell
  .\scripts\use-local-embedding-model.ps1
  ```

  脚本会生成 `data/model-cache/local-model.properties`，knowledge 启动时自动加载（从模块目录或仓库根目录启动都可以）；
  没有该文件时回退到远程模型。也可用 `EMBEDDING_MODEL_URI` / `EMBEDDING_TOKENIZER_URI` 覆盖路径。
- 该默认模型以英文语料为主，中文检索效果有限，属于 [roadmap](roadmap.md) Phase 3 的改造点。

## 多模型配置

- `app.ai.providers` 下每个条目是一个 **OpenAI 兼容端点**，启动时由 `ChatProviderRegistry` 构建独立的 `ChatClient`。
- 默认两个提供方 ID：`deepseek`、`deepseek-flash`；密钥统一 `OPENAI_API_KEY` / `OPENAI_BASE_URL`（见 `backend/ai-service-llm/README.md`）。
- 新增供应商（Qwen / Moonshot / OpenAI / Ollama 等）只需在 `application.yml` 的 `app.ai.providers` 增加一条：
  `base-url`、`api-key`、`model`。原生协议（Anthropic/Gemini）需要另写适配器。
- 前端对话页的模型下拉来自 `GET /api/chat/providers`，请求体用 `provider` 字段选择；接口**不返回密钥**。
- 当前每个 provider 的 key 仍在 `ai-service-llm`，符合 [data-security.md](standards/data-security.md) 的密钥边界。

## 鉴权（Sa-Token）

详见 [standards/gateway-auth.md](standards/gateway-auth.md)。摘要：

| 层级 | 职责 |
|------|------|
| **网关** `ai-gateway` | 唯一做**登录校验**：`app.gateway.auth.whitelist` 免登录，`blacklist` 一律 403，其余须带 `Authorization: Bearer` |
| **业务服务** | 仅 `@SaCheckPermission` 等注解鉴权 + `StpUtil` 取登录态；**不再**全局 `checkLogin` |
| **配置** | Token 参数统一在 `ai-common` → `config/satoken-common.yml`，各服务 `spring.config.import` 引入 |

直连业务端口（9201 等）绕过网关时不会挡未登录请求，生产应只暴露 9100。

## API 示例

```powershell
$login = Invoke-RestMethod -Method Post -Uri "http://localhost:9100/auth/login" `
  -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
Invoke-RestMethod -Method Post -Uri "http://localhost:9100/api/chat" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $($login.token)" } `
  -Body '{"message":"你好","mode":"AUTO"}'
```

## 管理端 API

| 接口 | 权限 | 说明 |
|------|------|------|
| `GET /api/admin/users` | `sys:user:manage` | 用户 + 角色 + 状态 |
| `PUT /api/admin/users/{id}/status` | `sys:user:manage` | 启用/禁用（不能禁用自己） |
| `PUT /api/admin/users/{id}/roles` | `sys:user:manage` | 分配角色（不能移除自己的管理员角色） |
| `GET /api/admin/roles` | `sys:role:manage` | 角色 + 权限码 |
| `PUT /api/admin/roles/{id}/permissions` | `sys:role:manage` | 分配权限 |
| `GET /api/admin/permissions` | `sys:role:manage` | 权限选项 |
| `GET /api/admin/menus` | `sys:menu:manage` | 菜单树 |
| `POST/PUT/DELETE /api/admin/menus...` | `sys:menu:manage` | 新增/编辑/删除菜单（有子菜单时拒绝删除） |

普通用户调用返回 403；网关已增加 `/api/admin/**` → `ai-auth` 路由。前端 `/system/users|roles|menus` 已接入。

## 限流与错误处理

- 对话按用户限流：默认 **20 次/分钟、200 次/天**，Redis 计数（`rate:chat:*`）。
- 可用 `CHAT_RATE_LIMIT_ENABLED` / `CHAT_RATE_LIMIT_PER_MINUTE` / `CHAT_RATE_LIMIT_PER_DAY` 覆盖。
- 超限返回 `429` + `Retry-After`，`detail` 是中文提示；模型认证失败/不可用返回 `502/503` 可读提示。
- 原始异常、下游状态码只写服务端日志，不把堆栈或上游错误码直接抛给前端。

## 测试

```powershell
mvn clean verify
```

Docker 集成测试：`mvn -pl backend/ai-auth -am test -DexcludedGroups=`（覆盖默认排除的 `docker` 标签，需本机 Docker）。
