# ai-service-conversation

会话与消息持久化（端口 **9203**，MySQL 表 `chat_session` / `chat_message`）。

| 方法 | 路径 |
|------|------|
| POST | `/api/conversation/sessions` |
| GET | `/api/conversation/sessions/{id}/messages` |
| POST | `/api/conversation/sessions/{id}/messages` |

由 `ai-service-llm` 在对话时自动创建/写入；前端后续可增加历史页直接调用 GET。
