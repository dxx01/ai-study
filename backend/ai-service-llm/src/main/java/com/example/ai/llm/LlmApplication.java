package com.example.ai.llm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 大模型微服务启动类。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@SpringBootApplication
public class LlmApplication {

    public static void main(String[] args) {
        SpringApplication.run(LlmApplication.class, args);
    }
}
