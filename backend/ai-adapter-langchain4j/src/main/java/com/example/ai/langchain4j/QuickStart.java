package com.example.ai.langchain4j;

import com.example.ai.langchain4j.config.AppConfigLoader;
import com.example.ai.langchain4j.config.AppConfigLoader.AppSettings;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public final class QuickStart {

    private QuickStart() {
    }

    public static void main(String[] args) {
        AppSettings settings;
        try {
            settings = AppConfigLoader.load();
        } catch (IllegalStateException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
            return;
        }

        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(settings.apiKey())
                .baseUrl(settings.baseUrl())
                .modelName(settings.model())
                .temperature(settings.temperature())
                .build();

        String question = "用三句话说明 LangChain4j 在 Java 项目里解决什么问题。";
        String answer = chatModel.chat(question);

        System.out.println("环境: " + settings.profile());
        System.out.println("问题: " + question);
        System.out.println("回答: " + answer);
    }
}
