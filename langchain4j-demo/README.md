# langchain4j-demo

不依赖 Spring 的最小 LangChain4j 示例：在命令行里完成一次对话。

## 配置

```bash
cp .env.example .env
```

PowerShell 示例：

```powershell
$env:OPENAI_API_KEY = "sk-..."
$env:OPENAI_BASE_URL = "https://api.openai.com"   # 可选
$env:OPENAI_CHAT_MODEL = "gpt-4o-mini"            # 可选
```

## 运行

```bash
mvn -q exec:java
```

未设置 `OPENAI_API_KEY` 时会打印友好提示并退出，不会发起网络请求。

## 下一步可练习

- 用 `AiServices` 定义带 `@SystemMessage` 的接口
- 接入 Embedding + 内存向量库做 RAG
- 对比与 Spring AI 在 API 设计上的异同
