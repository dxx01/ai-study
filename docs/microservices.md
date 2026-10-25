# 微服务运行说明

## 服务与端口

| 服务 | 模块 | 端口 |
|------|------|------|
| 网关 | `backend/ai-gateway` | 8080 |
| 认证 | `backend/ai-auth` | 9101 |
| LLM | `backend/ai-service-llm` | 9201 |
| 知识库 | `backend/ai-service-knowledge` | 9202 |
| 会话 | `backend/ai-service-conversation` | 9203 |

## 数据库

- **逻辑模型**：[database/schema.md](database/schema.md)  
- **本地 MySQL**：[database/local-development.md](database/local-development.md)  
- **Docker 部署**：[database/deployment.md](database/deployment.md)  

表结构以 `deploy/sql/02-schema.sql` 为准（前期手工 DDL，不用 Flyway）。Docker 首次启动（空数据卷）会自动执行 `01 → 01b → 02 → 03`；已有数据卷或本地 MySQL 用 `scripts/init-mysql-local.ps1` 初始化。

## 启动顺序

```powershell
.\scripts\use-jdk17.ps1
# 基础设施：Docker 见 deployment.md；或本地 init-mysql-local.ps1 + load-local-mysql-env.ps1

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
- 默认带两个示例：`deepseek`（`deepseek-chat`）和 `deepseek-flash`；切换默认用 `AI_DEFAULT_PROVIDER`。
- 新增供应商（Qwen / Moonshot / OpenAI / Ollama 等）只需在 `application.yml` 的 `app.ai.providers` 增加一条：
  `base-url`、`api-key`、`model`。原生协议（Anthropic/Gemini）需要另写适配器。
- 前端对话页的模型下拉来自 `GET /api/chat/providers`，请求体用 `provider` 字段选择；接口**不返回密钥**。
- 当前每个 provider 的 key 仍在 `ai-service-llm`，符合 [data-security.md](standards/data-security.md) 的密钥边界。

## API 示例

```powershell
$login = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/auth/login" `
  -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/chat" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $($login.token)" } `
  -Body '{"message":"你好","mode":"AUTO"}'
```

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
