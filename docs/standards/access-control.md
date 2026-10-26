# 权限模型（RBAC）

> 单租户、早期产品；不做 ABAC / 多租户。目标是：**管理员和普通用户分离，权限可配置，菜单可控，后端可校验**。

## 1. 结论：用 RBAC + 菜单

```
用户 ──< 用户角色 >── 角色 ──< 角色权限 >── 权限
                         └──< 角色菜单 >── 菜单
```

- **角色**：`ADMIN`、`USER`（以后可加 `AUDITOR` 等）。
- **权限**：细粒度动作码，后端 `@SaCheckPermission` 校验。
- **菜单**：导航/可见性，前端根据角色菜单渲染；菜单可以绑定一个权限码。
- **用户与角色、角色与权限、角色与菜单**都是多对多。

不建议只用 `sys_user.roles/permissions` JSON：

- JSON 适合“固定两三个角色”的 PoC，但无法做角色后台、菜单配置、反查“谁有某个权限”。
- 本项目的上传/重建索引需要明确只给管理员，后续还要加用户/角色管理页面，JSON 会越来越难维护。

也不建议现在上 ABAC/策略引擎：规则复杂、调试成本高，当前只有管理员/普通用户两类，RBAC 足够。

## 2. 表设计

系统表统一 `sys_` 前缀。

| 表 | 作用 |
|----|------|
| `sys_user` | 用户 |
| `sys_role` | 角色（`ADMIN` / `USER`） |
| `sys_permission` | 权限码（API/按钮级动作） |
| `sys_menu` | 菜单/导航（目录、页面） |
| `sys_user_role` | 用户-角色 |
| `sys_role_permission` | 角色-权限 |
| `sys_role_menu` | 角色-菜单 |

### 权限码约定

| 权限码 | 含义 | 默认角色 |
|--------|------|----------|
| `ai:chat` | 对话 | ADMIN / USER |
| `ai:knowledge:read` | 查看知识库 | ADMIN / USER |
| `ai:knowledge:write` | 上传资料 | **仅 ADMIN** |
| `ai:knowledge:admin` | 重建索引/删除文档 | **仅 ADMIN** |
| `sys:user:manage` | 用户管理 | ADMIN |
| `sys:role:manage` | 角色管理 | ADMIN |
| `sys:menu:manage` | 菜单管理 | ADMIN |

上传资料必须**后端**校验 `ai:knowledge:write`，不能只靠前端隐藏按钮。

### 菜单建议

| 菜单 | 路径 | 绑定权限 |
|------|------|----------|
| 智能对话 | `/chat` | `ai:chat` |
| 知识库 | `/knowledge` | `ai:knowledge:read` |
| 系统管理（目录） | `/system` | 仅 ADMIN |
| 用户管理 | `/system/users` | `sys:user:manage` |
| 角色管理 | `/system/roles` | `sys:role:manage` |
| 菜单管理 | `/system/menus` | `sys:menu:manage` |

## 3. 运行时怎么用

1. 登录成功后，按 `user_id` 查 `sys_user_role → sys_role → sys_role_permission → sys_permission`，把角色/权限写入 Sa-Token Session（Redis）。
2. 如果 RBAC 关系表为空（旧数据），回退到 `sys_user.roles/permissions` JSON，保证系统可用。
3. 网关只校验登录（白名单 / 黑名单见 [gateway-auth.md](gateway-auth.md)）；业务服务用 `@SaCheckPermission("ai:knowledge:write")` 做细粒度校验。
4. 登录响应和 `/auth/me` 都返回 `menus` 树，前端 `SidebarMenu` 直接渲染；按钮用权限码控制显隐。
5. 角色/权限变更后，让相关用户重新登录或主动刷新 Session，避免旧权限残留。

## 4. 与现有 JSON 字段的关系

`sys_user.roles/permissions` 保留为**迁移期 fallback**：

- 正常路径已改为从 `sys_*` 关联表读取。
- 关系表为空或表不存在时（旧库），回退 JSON，避免登录不可用。
- 注册时会自动绑定 `USER` 角色；菜单按角色返回。
- 等菜单/角色管理页面稳定后，再评估是否删除 JSON 列。

## 5. 后续可扩展

- 角色继承、数据权限（本人/本部门）、操作审计。
- 如果以后真的多租户，再引入 `tenant_id`，不要在单租户阶段提前加。
