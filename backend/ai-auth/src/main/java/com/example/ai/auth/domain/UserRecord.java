package com.example.ai.auth.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用户只读视图（认证层对外使用，不含 JPA 细节）。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
public final class UserRecord {

    private final String id;
    private final String username;
    private final String passwordHash;
    private final List<String> roles;
    private final List<String> permissions;

    public UserRecord(String id, String username, String passwordHash, List<String> roles, List<String> permissions) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.roles = Collections.unmodifiableList(new ArrayList<>(roles));
        this.permissions = Collections.unmodifiableList(new ArrayList<>(permissions));
    }

    public String id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public List<String> roles() {
        return roles;
    }

    public List<String> permissions() {
        return permissions;
    }
}
