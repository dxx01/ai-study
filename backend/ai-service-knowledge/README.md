# ai-service-knowledge

知识库与 RAG（端口 **9202**）。

| 方法 | 路径 | 权限 |
|------|------|------|
| POST | `/api/knowledge/search` | `ai:chat` |
| POST | `/api/knowledge/documents` | `ai:knowledge:write` |
| GET | `/api/knowledge/documents` | `ai:knowledge:read` |
| POST | `/api/knowledge/reload` | `ai:knowledge:admin` |

建议在模块根目录启动，以便 `./data/knowledge-base` 路径稳定。
