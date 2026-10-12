package com.example.ai.springai.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一名耐心的 Java 与 AI 应用助教，回答简洁、可执行。")
                .build();
    }

    @GetMapping
    public ChatResponse chat(@RequestParam(defaultValue = "你好") String message) {
        String reply = chatClient.prompt()
                .user(message)
                .call()
                .content();
        return new ChatResponse(message, reply);
    }

    public record ChatResponse(String question, String answer) {
    }
}
