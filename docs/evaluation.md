# 评测（Evaluation）

> 目标：每次改 prompt / chunk / embedding / topK / rerank，都能用同一套问题集回答“变好了还是变坏了”。

## 1. 准备数据

1. 启动全栈（见 [README](../README.md)）。
2. 上传示例资料 [docs/samples/hypertension-diet.md](samples/hypertension-diet.md) 到知识库。
3. 金标问题在 [eval/questions.jsonl](../eval/questions.jsonl)，一行一条：

```json
{
  "id": "salt-01",
  "question": "高血压人群每天食盐摄入建议不超过多少克？",
  "expected_source": "hypertension-diet.md",
  "expected_keywords": ["5 克", "5克"],
  "should_refuse": false
}
```

- `expected_source`：回答应引用的文档名；没有期望引用填 `null`。
- `expected_keywords`：答案中应出现的关键词（任意命中即可）。
- `should_refuse`：是否属于“应拒答/建议就医”的问题。

当前只有 10 条种子问题，**v0.5 目标扩到 20–30 条**，覆盖：资料内事实、资料外问题、急症、诊断/处方、联网时效问题。

## 2. 运行

```powershell
python scripts/eval_chat.py --limit 3
python scripts/eval_chat.py --provider deepseek-flash
```

参数：

| 参数 | 说明 |
|------|------|
| `--base-url` | 默认 `http://127.0.0.1:8080`（走网关） |
| `--mode` | `LOCAL`（默认，隔离 RAG）/ `WEB` / `AUTO` |
| `--provider` | 指定模型提供方；不传用后端默认 |
| `--dataset` | 默认 `eval/questions.jsonl` |
| `--delay` | 每题间隔，默认 0.5s，避免触发限流 |
| `--output` | 报告路径；默认 `eval/report-时间戳.json` |

脚本只依赖 Python 标准库，不需要 `pip install`。

## 3. 指标

| 指标 | 含义 | v0.5 目标 |
|------|------|-----------|
| `ok_rate` | HTTP 200 比例 | 100% |
| `citation_rate` | 有引用的回答占比 | ≥ 0.8 |
| `keyword_hit_rate` | 答案命中金标关键词 | ≥ 0.8 |
| `refusal_ok` | 应拒答问题中正确拒答数 | ≥ 0.9 |
| `p50/p95_latency_s` | 端到端延迟 | p50 < 5s，p95 < 10s |

后续再补：

- `recall@k`：金标文档是否出现在检索 topK（需要检索接口/日志）
- `source_hit_rate`：引用是否命中期望文档（当前脚本已记录单题 `source_hit`，尚未聚合）
- `groundedness`：抽样人工/LLM judge 判断每句是否有依据
- `token_cost`：单题 token 与费用

## 4. 工作流

1. 先跑一次基线，保存报告。
2. **一次只改一个变量**（embedding / chunk / topK / rerank / prompt）。
3. 再跑同一数据集，对比报告。
4. 把结论写进 [learning-log.md](learning-log.md)：改了什么、指标前后、为什么。

## 5. 已知局限

- 关键词命中是粗指标，不能替代人工/LLM judge。
- 当前数据集只覆盖示例文档，不能代表真实知识库。
- 没有自动上传文档，需先手工上传示例资料。
- 走网关时会受限流影响；评测脚本已加 `--delay`，必要时调大限流阈值。
