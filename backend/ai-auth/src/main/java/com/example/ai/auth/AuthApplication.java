package com.example.ai.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 认证服务入口：负责登录、注销与 Sa-Token 会话写入 Redis。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
@SpringBootApplication
@MapperScan("com.example.ai.auth.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
