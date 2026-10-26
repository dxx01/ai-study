package com.example.ai.auth.config;

import com.example.ai.auth.service.UserStoreService;
import com.example.ai.common.security.AiPermissions;
import com.example.ai.common.security.DemoPasswords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 内置演示账号种子（仅当库中不存在时插入）。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@Component
public class AuthUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthUserSeeder.class);

    private static final List<String> ADMIN_ROLES = List.of("admin");
    private static final List<String> ADMIN_PERMISSIONS = Arrays.asList(
            AiPermissions.CHAT,
            AiPermissions.KNOWLEDGE_READ,
            AiPermissions.KNOWLEDGE_WRITE,
            AiPermissions.KNOWLEDGE_ADMIN,
            AiPermissions.SYS_USER_MANAGE,
            AiPermissions.SYS_ROLE_MANAGE,
            AiPermissions.SYS_MENU_MANAGE
    );

    private static final List<String> USER_ROLES = List.of("user");
    private static final List<String> USER_PERMISSIONS = Arrays.asList(
            AiPermissions.CHAT,
            AiPermissions.KNOWLEDGE_READ
    );

    private final UserStoreService userStoreService;

    public AuthUserSeeder(UserStoreService userStoreService) {
        this.userStoreService = userStoreService;
    }

    @Override
    public void run(ApplicationArguments args) {
        userStoreService.ensureBuiltInUser("admin", DemoPasswords.ADMIN_PASSWORD, ADMIN_ROLES, ADMIN_PERMISSIONS);
        userStoreService.ensureBuiltInUser("user", DemoPasswords.USER_PASSWORD, USER_ROLES, USER_PERMISSIONS);
        log.info("内置用户种子检查完成（admin / user）");
    }
}
