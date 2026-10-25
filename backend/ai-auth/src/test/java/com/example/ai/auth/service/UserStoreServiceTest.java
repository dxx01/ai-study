package com.example.ai.auth.service;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.example.ai.common.security.AiPermissions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户仓储与注册逻辑测试（H2 + MyBatis-Plus）。
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
@SpringBootTest(classes = UserStoreServiceTest.TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class UserStoreServiceTest {

    @Autowired
    private UserStoreService userStoreService;

    @Test
    void registerAndFindUser() {
        var user = userStoreService.register("demo02", "demo123");
        assertThat(user.username()).isEqualTo("demo02");
        assertThat(user.permissions()).contains(AiPermissions.CHAT);

        var stored = userStoreService.findByUsername("demo02").orElseThrow();
        assertThat(stored.roles()).contains("user");
        assertThat(stored.permissions()).contains(AiPermissions.CHAT);
        assertThat(userStoreService.matchesPassword("demo02", "demo123")).isTrue();
    }

    @Configuration
    @MapperScan("com.example.ai.auth.mapper")
    @Import({UserStoreService.class, com.example.ai.auth.config.AuthPasswordConfig.class,
            com.example.ai.common.mybatis.AuditMetaObjectHandler.class})
    @ImportAutoConfiguration({
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            SqlInitializationAutoConfiguration.class,
            MybatisPlusAutoConfiguration.class
    })
    static class TestConfig {
    }
}
