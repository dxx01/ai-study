# spring-ai-demo

最小 Spring Boot + Spring AI 示例：通过 HTTP 调用大模型。

## 配置

复制环境变量模板（勿提交真实密钥）：

```bash
cp .env.example .env
```

或在 IDE / 终端中设置：

- `OPENAI_API_KEY`：API 密钥
- `OPENAI_BASE_URL`（可选）：兼容 OpenAI 的网关，例如 DeepSeek

`application.yml` 中已用 `${OPENAI_API_KEY}` 占位，启动前请确保变量已注入。

## 运行

```bash
mvn spring-boot:run
```

## 试用

```bash
curl "http://localhost:8080/api/chat?message=用三句话介绍Spring%20AI"
```

## 下一步可练习

- 增加 `system` 提示词与对话记忆
- 接入 `VectorStore` 做 RAG
- 使用 `@Tool` / Function Calling
