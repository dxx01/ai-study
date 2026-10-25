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
- 网络受限时（国内访问 GitHub raw 常见）会卡在 `Caching the URL ...`；可配置代理，或预下载 `model.onnx` / `tokenizer.json` 后设置
  `spring.ai.embedding.transformer.onnx.model-uri=file:/...` 与 `spring.ai.embedding.transformer.tokenizer.uri=file:/...`。
- 该默认模型以英文语料为主，中文检索效果有限，属于 [roadmap](roadmap.md) Phase 3 的改造点。

## API 示例

```powershell
$login = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/auth/login" `
  -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/chat" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $($login.token)" } `
  -Body '{"message":"你好","mode":"AUTO"}'
```

## 测试

```powershell
mvn clean verify
```

Docker 集成测试：`mvn -pl backend/ai-auth -am test -DexcludedGroups=`（覆盖默认排除的 `docker` 标签，需本机 Docker）。
