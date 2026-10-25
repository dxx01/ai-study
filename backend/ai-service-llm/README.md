# ai-service-llm

大模型接入与对话编排（端口 **9201**）。

- `POST /api/chat` — 对话（需 `ai:chat`）
- 编排：conversation 历史 → knowledge 检索 → 可选 Tavily → Spring AI ChatClient

## 配置

- `application-dev.yml` 导入 `./.env.dev.properties`（含 `OPENAI_API_KEY` 等）
- 下游地址：`app.services.knowledge-base-url`、`conversation-base-url`

网关路径：`http://localhost:8080/api/chat`。
