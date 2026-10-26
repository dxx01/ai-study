# ai-service-llm

大模型接入与对话编排（端口 **9201**）。

- `POST /api/chat` — 对话（需 `ai:chat`）
- 编排：conversation 历史 → knowledge 检索 → 可选 Tavily → Spring AI ChatClient

## 模型密钥（OpenAI 兼容）

环境变量统一 **`OPENAI_*`**（与 Spring AI 命名一致），写在 gitignore 的 properties 里：

| 变量 | 含义 |
|------|------|
| `OPENAI_API_KEY` | API Key |
| `OPENAI_BASE_URL` | 默认 `https://api.deepseek.com` |
| `OPENAI_CHAT_MODEL` | 提供方 `deepseek` 的模型名 |
| `OPENAI_CHAT_MODEL_FLASH` | 提供方 `deepseek-flash` 的模型名 |

`app.ai.providers` 里的 **`deepseek` / `deepseek-flash`** 是内部提供方 ID（前端下拉用），不是环境变量名。

### 何时加载 `.env.dev.properties` / `.env.prod.properties`

| Spring profile | 来源 | 额外加载 |
|----------------|------|----------|
| **`dev`** | `deploy/env/local.env` → `current.env` | `application-dev.yml` → **`.env.dev.properties`** |
| **`server`** | `deploy/env/server.env` → `current.env` | `application-server.yml` → **`.env.dev.properties`**（本机连云库调试） |
| **`prod`** | 需显式 `spring.profiles.active=prod` | `application-prod.yml` → **`.env.prod.properties`** |

日常 `use-env.ps1 local|server` 只会是 **dev** 或 **server**，不会自动进 **prod**；`application-prod.yml` 仅在你把 profile 设为 `prod` 时生效。

在 **`backend/ai-service-llm/`** 下创建 `.env.dev.properties` 示例：

```properties
OPENAI_API_KEY=sk-xxx
OPENAI_BASE_URL=https://api.deepseek.com
OPENAI_CHAT_MODEL=deepseek-chat
OPENAI_CHAT_MODEL_FLASH=deepseek-flash
```

## 其他配置

- Nacos：`ai-service-llm.yaml`（限流、默认 provider 等，不含 Key）
- 下游：`app.services.knowledge-base-url`、`conversation-base-url`（`application.yml`）

网关：`http://localhost:9100/api/chat`。
