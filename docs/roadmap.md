# 分阶段路线图

> 范围、非目标与验收指标见 [project-charter.md](project-charter.md)；本文只列任务。

## Phase 0 — 基线 ✅

- [x] 微服务拆分、backend/frontend
- [x] 手工 DDL + 数据规范（docs/standards）
- [x] 数据库文档分本地 / 部署 / schema

## Phase 1 — 持久化与稳定

- [x] auth / conversation 建表脚本与测试
- [ ] 知识库文档元数据 MySQL
- [ ] 前端会话历史页

## Phase 2 — 流式与 AI 端口

- [ ] SSE、ai-core-port

## Phase 3 — RAG 生产化

- [ ] 外置向量库、rerank

## Phase 4+ — Agent、LangChain4j 适配

见历史规划；以 [AGENTS.md](../AGENTS.md) 为准。
