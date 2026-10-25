package com.example.ai.conversation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 会话微服务启动类。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@SpringBootApplication
@MapperScan("com.example.ai.conversation.mapper")
public class ConversationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConversationApplication.class, args);
    }
}
