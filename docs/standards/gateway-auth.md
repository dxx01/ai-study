# 网关鉴权（Sa-Token）

## 原则

| 位置 | 做什么 | 不做什么 |
|------|--------|----------|
| **ai-gateway（9100）** | 未登录不放行；白名单免登录；黑名单一律拒绝 | 不校验 `ai:knowledge:write` 等业务权限码 |
| **ai-auth / 各业务服务** | `@SaCheckPermission`、从 Session 取 `loginId` | **不再**注册全局 `checkLogin` 拦截器 |
| **ai-common** | 一份 `config/satoken-common.yml`（Token 名、前缀、超时等） | 不决定「谁必须登录」 |

生产只对外暴露 **9100**；直连 9101/920x 会绕过网关登录校验，仅限本机开发调试。

## 配置位置

1. **Token 行为（全服务一致）**  
   `backend/ai-common/src/main/resources/config/satoken-common.yml`  
   各服务 `application*.yml` 中：

   ```yaml
   spring:
     config:
       import: optional:classpath:config/satoken-common.yml
   ```

2. **谁免登录 / 谁禁止访问（仅网关）**  
   `backend/ai-gateway/src/main/resources/application.yml`（或 Nacos 里覆盖 `ai-gateway` 配置）：

   ```yaml
   app:
     gateway:
       auth:
         whitelist:
           - /auth/login
           - /auth/register
           - /actuator/**
         blacklist: []
   ```

   - **whitelist**：Ant 风格路径，匹配则**不**做 `checkLogin`（对外 URL，与 Gateway `Path` 一致，如 `/auth/**` 不是下游 `/login`）。
   - **blacklist**：匹配则直接 403（在已排除白名单之外的流量上仍会先走黑名单判断；典型用法是封禁敏感路径）。
   - **其余路径**：请求头须带 `Authorization: Bearer <token>`，否则网关返回 **401** 与统一 JSON（见 `SaTokenGatewayConfig`）。

3. **Nacos 公共配置**  
   `deploy/nacos/configs/application-common.yaml` 只 `import` classpath 的 `satoken-common.yml`，**不要**在各服务 YAML 里再复制一整段 `sa-token:`。

## 与 RBAC 的关系

登录与权限加载在 **ai-auth**；网关只验证「有没有有效 Session」。  
接口级权限见 [access-control.md](access-control.md)：`@SaCheckPermission` 写在 Controller/Service，由各业务 JVM 内的 Sa-Token MVC 拦截器处理。

## 运维检查清单

- [ ] 新增对外 API 时：默认需登录；仅登录/注册/健康检查等加入 `whitelist`。
- [ ] 临时下线接口：可加入 `blacklist`，而不是删路由。
- [ ] 改 `satoken-common.yml` 后需重启**所有**持有 Session 的服务（含网关与 auth）。
- [ ] 安全组 / 防火墙不开放 9201–9203、9101。
