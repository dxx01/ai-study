# Nacos（Docker + MySQL）

服务器：`47.120.50.85`  
容器：`ai-study-nacos`（`nacos/nacos-server:v2.4.3`）  
端口：`8848`（HTTP/Console）、`9848`（gRPC）、`9849`（gRPC）

## 存储

Nacos 使用 MySQL 库 `nacos_config`（表结构见 `mysql-schema.sql`），MySQL 容器内账号 `nacos`，密码见 gitignored 的 `../env/server.env`（`NACOS_DB_PASSWORD`）。

首次部署需要：

```bash
# 在服务器上，MySQL 已启动
docker exec ai-study-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" \
  -e "CREATE DATABASE IF NOT EXISTS nacos_config DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
docker exec -i ai-study-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" nacos_config < mysql-schema.sql
```

并确保 `nacos` 用户对 `nacos_config` 有权限、`users` 表有 `nacos` 管理员（`enabled=1`）。

## 配置发布

```bash
cd /opt/ai-study/deploy/nacos
# 账号密码与微服务相同，见 deploy/env/current.env（NACOS_USERNAME / NACOS_PASSWORD）
bash publish-configs.sh
```

配置 Data ID：

| Data ID | 内容 |
|---------|------|
| `application-common.yaml` | sa-token / MyBatis-Plus / actuator / logging 公共配置 |
| `ai-auth.yaml` | auth 日志级别 |
| `ai-gateway.yaml` | 网关 `lb://` 路由 |
| `ai-service-llm.yaml` | 模型 provider、限流、联网检索开关（Key 用环境变量占位） |
| `ai-service-knowledge.yaml` | RAG topK / 阈值 / embedding model |
| `ai-service-conversation.yaml` | conversation 日志级别 |

命名空间使用 **public（tenant 为空）**；Group `DEFAULT_GROUP`。

## 本机访问

服务器安全组未放行 8848/9848 时，本机用 SSH 隧道：

```powershell
.\scripts\start-nacos-tunnel.ps1
# 另开窗口
.\scripts\switch-profile.ps1 -Target server
```

`NACOS_SERVER_ADDR` 建议 `47.120.50.85:8848`（安全组放行 8848/9848/9849）。本机：`.\scripts\check-nacos.ps1`、`.\scripts\publish-nacos-configs.ps1` 同步仓库配置到远端。

## 安全

- Nacos 开启了鉴权（`NACOS_AUTH_ENABLE=true`），token/identity 在 `server.env` 底部 Docker 段。
- 控制台账号在 `deploy/env/local.env` / `server.env` 的 `NACOS_*`（已 gitignore）；改 Nacos 密码后须同步 env 并重启微服务。
- 不要把 Nacos 密码、token 写入 Git；`server.*.env` 已 gitignore。
