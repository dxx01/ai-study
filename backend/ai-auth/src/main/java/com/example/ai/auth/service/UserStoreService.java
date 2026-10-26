package com.example.ai.auth.service;

import com.example.ai.auth.domain.UserEntity;
import com.example.ai.auth.domain.UserRecord;
import com.example.ai.auth.mapper.SysRbacMapper;
import com.example.ai.auth.mapper.UserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.ai.common.mybatis.DbTimeZones;
import com.example.ai.common.security.AiPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户读写（MyBatis-Plus）。
 *
 * @author 邓鑫鑫
 * @since 0.4.0
 */
@Service
public class UserStoreService {

    private static final Logger log = LoggerFactory.getLogger(UserStoreService.class);

    private static final List<String> DEFAULT_USER_ROLES = List.of("user");
    private static final List<String> DEFAULT_USER_PERMISSIONS = List.of(
            AiPermissions.CHAT,
            AiPermissions.KNOWLEDGE_READ
    );

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SysRbacMapper rbacMapper;

    public UserStoreService(UserMapper userMapper, PasswordEncoder passwordEncoder, SysRbacMapper rbacMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.rbacMapper = rbacMapper;
    }

    @Transactional(readOnly = true)
    public Optional<UserRecord> findByUsername(String username) {
        UserEntity entity = userMapper.selectByUsername(username);
        return Optional.ofNullable(entity).map(this::toRecord);
    }

    @Transactional(readOnly = true)
    public Optional<UserRecord> findByLoginId(String loginId) {
        try {
            long id = Long.parseLong(loginId);
            UserEntity entity = userMapper.selectById(id);
            return Optional.ofNullable(entity).map(this::toRecord);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public boolean matchesPassword(String username, String rawPassword) {
        return findByUsername(username)
                .map(user -> passwordEncoder.matches(rawPassword, user.passwordHash()))
                .orElse(false);
    }

    @Transactional
    public void recordLogin(long userId) {
        LocalDateTime now = LocalDateTime.now(DbTimeZones.SHANGHAI);
        userMapper.update(null, Wrappers.<UserEntity>lambdaUpdate()
                .eq(UserEntity::getId, userId)
                .set(UserEntity::getLastLoginAt, now)
                .set(UserEntity::getUpdatedAt, now));
    }

    private UserRecord toRecord(UserEntity entity) {
        List<String> roles = entity.getRoles();
        List<String> permissions = entity.getPermissions();
        try {
            List<String> rbacRoles = rbacMapper.selectRoleCodes(entity.getId());
            List<String> rbacPermissions = rbacMapper.selectPermissionCodes(entity.getId());
            if (!rbacRoles.isEmpty() || !rbacPermissions.isEmpty()) {
                roles = rbacRoles;
                permissions = rbacPermissions;
            }
        } catch (DataAccessException ex) {
            log.debug("RBAC 表不可用，回退 sys_user JSON 权限: userId={}", entity.getId(), ex);
        }
        return entity.toRecord(roles, permissions);
    }

    @Transactional
    public UserRecord register(String username, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
        if (userMapper.selectByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode(rawPassword));
        entity.setRoles(DEFAULT_USER_ROLES);
        entity.setPermissions(DEFAULT_USER_PERMISSIONS);
        userMapper.insert(entity);
        try {
            rbacMapper.assignDefaultRole(entity.getId());
        } catch (DataAccessException ex) {
            log.debug("RBAC 表不可用，跳过默认角色绑定: userId={}", entity.getId(), ex);
        }
        return toRecord(entity);
    }

    @Transactional
    public void ensureBuiltInUser(String username, String rawPassword, List<String> roles, List<String> permissions) {
        if (userMapper.selectByUsername(username) != null) {
            return;
        }
        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode(rawPassword));
        entity.setRoles(roles);
        entity.setPermissions(permissions);
        userMapper.insert(entity);
    }
}
