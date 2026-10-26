# 字典与状态字段规范

适用于本仓库 **MySQL + 后端 + 前端** 的枚举型字段（状态、类型等）。与 [database-design.md](database-design.md) 配套使用。

## 1. 原则

| 规则 | 说明 |
|------|------|
| **存库用数字** | 优先 `TINYINT`（无符号可选），取值 **0、1、2、3…** 递增，禁止在业务表用 `ACTIVE`/`DISABLED` 等英文字符串 |
| **注释写清含义** | 列 `COMMENT` 列出取值，例如：`启用状态：0=禁用，1=启用` |
| **展示用中文** | 界面只显示字典标签（启用、禁用），不直接展示原始码 |
| **单一事实来源** | 同一套字典在后端 `ai-common`、前端 `constants/dicts.ts` 各维护一份；取值与注释必须与 DDL 一致 |
| **多值字典** | 同一列若超过 2 个状态，仍从 **0** 起连续编号，在 COMMENT 与代码中完整列出 |

## 2. 标准字典（当前）

### 2.1 `enable_status`（通用启用/停用）

用于：`sys_user.status`、`sys_role.status`、`sys_menu.status` 等「是否可用」字段。

| 值 | 含义 | 后端常量 |
|----|------|----------|
| 0 | 禁用 | `EnableStatus.DISABLED` |
| 1 | 启用 | `EnableStatus.ENABLED` |

DDL 示例：

```sql
status TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态：0=禁用，1=启用'
```

### 2.2 其他业务字典（规划对齐，改表时一并落实）

| 字典 | 表/列 | 取值（示例） |
|------|--------|----------------|
| 消息处理状态 | `chat_message.status` | 0=待处理，1=流式中，2=已完成，3=失败 |
| 文档索引状态 | `knowledge_document.status` | 0=上传中，1=已索引，2=失败 |
| 知识资料类型 | `knowledge_document.category` | 0=未分类，1=食物营养，2=疾病科普，9=其他 |

尚未迁移为 `TINYINT` 的列，在 [schema.md](../database/schema.md) 中标注为待收敛项；新表/新列一律按本规范设计。

## 3. 前端

- 字典定义：`frontend/src/constants/dicts.ts`
- 列表/标签：使用 `AppStatusTag`（`dict="enable"`）或 `labelEnableStatus(code)`，禁止在页面写死 `ACTIVE` 文案
- 表单：`el-option` 的 `label` 为中文，`value` 为数字

## 4. 后端

- 通用启用/停用：`com.example.ai.common.dict.EnableStatus`
- API JSON 中状态字段类型为 **数字**（`0`/`1`），与库一致
- 校验：`@Min(0) @Max(1)` 或业务允许的区间

## 5. 变更流程

1. 在本文档登记字典取值  
2. 修改 `02-schema.sql` + 增量 SQL + `schema.md`  
3. 同步 Java 常量/枚举与前端 `dicts.ts`  
4. 审查清单见 [database-review-checklist.md](database-review-checklist.md) 字典相关项  

## 6. 运行时字典表（非目标）

当前 **不** 引入 `sys_dict_*` 表；字典量小且与代码强耦合，以代码 + DDL 注释为准。若未来需要运营可配字典，再单独评审。
