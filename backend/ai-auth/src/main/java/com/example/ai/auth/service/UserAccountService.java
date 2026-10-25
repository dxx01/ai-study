package com.example.ai.auth.service;

import com.example.ai.auth.domain.UserRecord;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用户账户门面（MySQL）。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
@Service
public class UserAccountService {

    private final UserStoreService userStore;

    public UserAccountService(UserStoreService userStore) {
        this.userStore = userStore;
    }

    /**
     * @param username 登录名
     * @return 用户
     */
    public Optional<UserRecord> findByUsername(String username) {
        return userStore.findByUsername(username);
    }

    /**
     * @param loginId 登录 ID
     * @return 用户
     */
    public Optional<UserRecord> findByLoginId(String loginId) {
        return userStore.findByLoginId(loginId);
    }

    /**
     * @param loginId 登录 ID
     * @return 权限码
     */
    public List<String> permissionsForLoginId(String loginId) {
        return userStore.findByLoginId(loginId)
                .map(UserRecord::permissions)
                .orElse(Collections.emptyList());
    }

    /**
     * @param loginId 登录 ID
     * @return 角色码
     */
    public List<String> rolesForLoginId(String loginId) {
        return userStore.findByLoginId(loginId)
                .map(UserRecord::roles)
                .orElse(Collections.emptyList());
    }

    /**
     * @param username 登录名
     * @param rawPassword 明文密码
     * @return 是否匹配
     */
    public boolean matchesPassword(String username, String rawPassword) {
        return userStore.matchesPassword(username, rawPassword);
    }

    /**
     * @param username 登录名
     * @param rawPassword 密码
     * @return 新注册用户
     */
    public UserRecord register(String username, String rawPassword) {
        return userStore.register(username, rawPassword);
    }
}
