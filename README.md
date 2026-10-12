# ai-study

面向 Java 开发者的 AI 应用学习仓库，逐步练习 **Spring AI** 与 **LangChain4j**。

## 目录结构

| 模块 | 说明 |
|------|------|
| [`spring-ai-demo`](spring-ai-demo/) | Spring Boot + Spring AI（ChatClient / REST 示例） |
| [`langchain4j-demo`](langchain4j-demo/) | 纯 Java + LangChain4j（命令行对话示例） |

## 环境要求

- JDK **17+**（推荐 21）
- Maven **3.9+**
- 兼容 OpenAI API 的密钥（或 DeepSeek、通义等，按各模块 README 配置）

## 快速开始

```bash
# 编译全部模块
mvn -q clean verify

# Spring AI Web 示例
cd spring-ai-demo
cp .env.example .env   # 填入 API Key 后
mvn spring-boot:run

# LangChain4j 命令行示例
cd langchain4j-demo
cp .env.example .env
mvn exec:java
```

## 建议学习路径

1. 先跑通 `langchain4j-demo`，理解 Model / Prompt / 一次对话调用链。
2. 再跑通 `spring-ai-demo`，对比 Spring 生态下的自动配置与 `ChatClient`。
3. 在本仓库新增分支或子模块，练习：RAG、Function Calling、多模型切换等。

## 参考文档

- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/)
- [LangChain4j Documentation](https://docs.langchain4j.dev/)

## 许可证

MIT（示例代码可自由修改用于学习）
