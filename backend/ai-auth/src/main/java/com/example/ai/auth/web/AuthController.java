package com.example.ai.auth.web;

import cn.dev33.satoken.stp.StpUtil;
import com.example.ai.auth.domain.UserRecord;
import com.example.ai.auth.service.SaTokenSessionHelper;
import com.example.ai.auth.service.SysMenuService;
import com.example.ai.auth.service.UserAccountService;
import com.example.ai.auth.web.dto.LoginRequest;
import com.example.ai.auth.web.dto.LoginResponse;
import com.example.ai.auth.web.dto.RegisterRequest;
import com.example.ai.auth.web.dto.UserInfoResponse;
import com.example.ai.common.api.dto.MenuItem;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证 API：登录、注册、注销、当前用户。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserAccountService userAccountService;
    private final SysMenuService menuService;

    public AuthController(UserAccountService userAccountService, SysMenuService menuService) {
        this.userAccountService = userAccountService;
        this.menuService = menuService;
    }

    /**
     * 用户名密码登录，返回 Bearer Token。
     *
     * @param request 登录参数
     * @return token 与权限列表
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UserRecord user = userAccountService.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (!userAccountService.matchesPassword(request.username(), request.password())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!com.example.ai.common.dict.EnableStatus.isEnabled(user.status())) {
            throw new IllegalArgumentException("账号已被禁用，请联系管理员");
        }
        userAccountService.recordLogin(Long.parseLong(user.id()));
        SaTokenSessionHelper.bindUserSession(user);
        return toLoginResponse(user, StpUtil.getTokenValue(), menusFor(user));
    }

    /**
     * 注册并自动登录。
     *
     * @param request 注册参数
     * @return token 与权限列表
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        UserRecord user = userAccountService.register(request.getUsername(), request.getPassword());
        SaTokenSessionHelper.bindUserSession(user);
        return toLoginResponse(user, StpUtil.getTokenValue(), menusFor(user));
    }

    /**
     * 注销当前 token。
     */
    @PostMapping("/logout")
    public void logout() {
        StpUtil.logout();
    }

    /**
     * @return 当前登录用户权限
     */
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserInfoResponse me() {
        String loginId = StpUtil.getLoginIdAsString();
        UserRecord user = userAccountService.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("用户不存在: " + loginId));
        return new UserInfoResponse(loginId, user.username(), user.roles(), user.permissions(), menusFor(user));
    }

    private List<MenuItem> menusFor(UserRecord user) {
        return menuService.menusForUser(Long.parseLong(user.id()));
    }

    private static LoginResponse toLoginResponse(UserRecord user, String token, List<MenuItem> menus) {
        return new LoginResponse(
                token,
                "Bearer",
                user.id(),
                user.username(),
                user.roles(),
                user.permissions(),
                menus
        );
    }
}
