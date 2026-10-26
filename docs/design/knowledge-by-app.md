# 知识库：按应用隔离 + 资料分类 + PDF 提取 Markdown

> 状态：**M1 已落地**（`sys_app`、`app_id`/`category`、API 过滤、路由 `/apps/:appId/knowledge`）；M2/M3 见下文。  
> 与 [project-charter.md](../project-charter.md) v0.5「知识库上传、列表、检索」一致；不做多空间 CMS、不做多租户计费。

## 1. 已确认决策

| # | 决策 |
|---|------|
| 1 | **同一应用一个库**；资料用 **类型/分类** 区分（如健康顾问：食物类、疾病类）。后续若真要「多库」，可再引入 `space_id`，本阶段不建。 |
| 2 | 前端走 **子路由** `/apps/:appId/knowledge`（与 `/apps/:appId/chat` 一致）。 |
| 3 | PDF→Markdown **先纯文本**：Tika 提取正文，写入 `.md`，不追求版式/表格还原。 |
| 4 | **按 app 各存一份**；同一文件 hash 可在不同 app 重复入库（不去重跨 app）。 |
| 5 | **已有文档** 迁移为 `app_id = health`。 |

## 2. 概念模型

```
平台
 └── 应用 app_id（如 health，对应 DEMO_APPS / 对话 expert）
      └── 知识库（逻辑上一个，不单独建表）
            └── 文档 knowledge_document
                  ├── category（资料类型：食物 / 疾病 / …）
                  ├── 原文件（pdf/docx/…）
                  └── extracted.md（PDF 等提取结果，可选）
```

- **检索 / RAG**：仅在该 `app_id` 的文档与向量 metadata 内搜索。  
- **对话**：`/apps/health/chat` 调用 knowledge 时固定带 `appId=health`（与 `expertId` 配置映射）。

## 3. 数据模型（草案）

### 3.1 `knowledge_document` 新增列

| 列 | 类型 | 说明 |
|----|------|------|
| `app_id` | `VARCHAR(32) NOT NULL` | 应用标识，与前端 `apps/:appId` 一致，如 `health` |
| `category` | `TINYINT NOT NULL DEFAULT 0` | 资料类型，见字典 `knowledge_doc_category` |
| `markdown_path` | `VARCHAR(512) NULL` | 提取后的 `.md` 绝对路径；上传即为 `.md` 时可与源文件相同或仅索引原文件 |
| `source_format` | `VARCHAR(16) NULL` | 原始扩展名归类：`pdf` / `md` / `docx` …（便于展示） |

索引建议：

- `idx_knowledge_document_app_created (app_id, created_at DESC)`
- `idx_knowledge_document_app_category (app_id, category)`

**迁移**：`UPDATE knowledge_document SET app_id = 'health' WHERE app_id IS NULL OR app_id = ''`（加列后执行）。

### 3.2 字典 `knowledge_doc_category`（健康应用首期）

| 值 | 含义 | 说明 |
|----|------|------|
| 0 | 未分类 | 默认；迁移历史数据可用 0 或批量改为具体类 |
| 1 | 食物营养 | 膳食、食材、营养素 |
| 2 | 疾病科普 | 症状、慢病生活方式等（非诊疗） |
| 9 | 其他 | 兜底 |

新应用可在 `docs/standards/dictionaries.md` 与前端 `dicts.ts` 扩展「按 app 的可选分类」；库表仍用 `TINYINT`，不同 app 可复用同一套或按 app 配置映射（v1 可先硬编码 health 三类）。

### 3.3 存储目录（建议）

```
{app.knowledge.storage-dir}/
  health/
    {documentId}/
      original.pdf          # 或 original 保留原扩展名
      extracted.md          # Tika 纯文本写入
```

向量 metadata 增加：`appId`、`category`（检索 filter 用）。

## 4. API（草案）

基路径仍为 `/api/knowledge`（网关 → ai-service-knowledge）。

| 方法 | 路径 | 变化 |
|------|------|------|
| GET | `/documents?appId=health&category=` | 列表按 app 过滤；可选 category |
| POST | `/upload` | multipart：`file` + **`appId`** + **`category`** |
| POST | `/search` | body：`query` + **`appId`**（LLM 与前端调试必传） |
| GET | `/documents/{id}/markdown` | 下载/预览 `extracted.md`（权限同 read） |

权限不变：`ai:knowledge:read/write/admin`；写操作校验 `appId` 合法（存在于平台应用注册表或配置白名单）。

## 5. 前端路由

| 路径 | 说明 |
|------|------|
| `/apps/:appId/knowledge` | 知识库页（从 `KnowledgeView` 复用，读路由 `appId`） |
| `/knowledge` | **301/redirect** → `/apps/health/knowledge`（兼容旧菜单） |

上传表单：选择 **资料类型**（字典中文）；列表可增加按类型筛选。

侧栏菜单：可将「知识库」改为进入 **当前应用** 或保留平台级入口跳转 `health`（与 `07-platform-nav.sql` 同步时再定）。

## 6. LLM 编排

- `KnowledgeServiceClient.search(query, appId, authorization)`  
- `ChatOrchestrationService`：从会话/专家配置解析 `appId`（如 `home-health-advisor` → `health`）。  
- 评测脚本 `eval_chat.py`：默认 `appId=health`。

## 7. PDF → Markdown（M2 行为）

1. 保存 `original.*`  
2. `TikaDocumentReader` 取文本（与现索引相同）  
3. 写 `extracted.md`（UTF-8，文首可加一行注释：`<!-- auto-extracted from {fileName} -->`）  
4. 分块与向量索引仍基于提取文本；**md 文件供人工核对与下载**

不上 OCR、不上大模型排版（扫描版 PDF 可能效果差，文档中说明）。

## 8. 实施顺序（与前期讨论一致）

| 阶段 | 内容 |
|------|------|
| **M1** | DDL + `app_id`/`category`；API 与检索过滤；路由 `/apps/:appId/knowledge`；迁移 health |
| **M2** | `markdown_path` + 上传 PDF 写 md + 下载接口 |
| **M3** | LLM search 带 `appId`；对话只命中本应用库 |

## 9. 非目标（本需求内不做）

- 同一应用下多个独立「知识库空间」表  
- 跨 app 共享文档、引用计数  
- 扫描 PDF 专用 OCR、版式级 Markdown  
- 知识库审批流、版本对比 UI  

---

实现前需同步：`deploy/sql/02-schema.sql`（或增量 `09-knowledge-app.sql`）、`schema.md`、`dictionaries.md`、实体与 Mapper、前端路由与菜单种子。
