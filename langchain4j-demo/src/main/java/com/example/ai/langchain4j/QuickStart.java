package com.example.ai.langchain4j;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * 最小 LangChain4j 对话示例。密钥通过环境变量注入，便于本地学习与 CI 跳过真实调用。
 */
public final class QuickStart {

    private QuickStart() {
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("请先设置环境变量 OPENAI_API_KEY（可参考 .env.example）。");
            System.exit(1);
        }

        String baseUrl = envOrDefault("OPENAI_BASE_URL", "https://api.openai.com");
        String model = envOrDefault("OPENAI_CHAT_MODEL", "gpt-4o-mini");

        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(0.7)
                .build();

        String question = "用三句话说明 LangChain4j 在 Java 项目里解决什么问题。";
        String answer = chatModel.chat(question);

        System.out.println("问题: " + question);
        System.out.println("回答: " + answer);
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
