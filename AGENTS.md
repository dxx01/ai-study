# AI 应用平台 — Agent 协作约定

## 微服务

| 模块 | 端口 |
|------|------|
| `backend/ai-gateway` | 8080 |
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
   - [docs/standards/data-access.md](docs/standards/data-access.md)  
   - [docs/standards/data-security.md](docs/standards/data-security.md)  
   交付前对照 [docs/standards/database-review-checklist.md](docs/standards/database-review-checklist.md)  
4. **库表文档**：逻辑结构 [docs/database/schema.md](docs/database/schema.md)；本地与部署分开见 [docs/database/README.md](docs/database/README.md)  
5. **结构变更**：同步 `deploy/sql/02-schema.sql` + MyBatis-Plus 实体 + schema.md（前期不用 Flyway）  
6. **构建**：JDK 17 → `.\scripts\use-jdk17.ps1`  

## 启动

见 [README.md](README.md)、[docs/microservices.md](docs/microservices.md)。
