# 阿里云服务器（Docker：MySQL + Redis）

公网示例：**47.120.50.85**（以你控制台实际 IP 为准）。

## 1. 安全提醒

- **勿**把 root 密码、`.env` 提交到 Git；`deploy/env/server.*.env` 已 gitignore。
- 生产建议在阿里云安全组中：**仅对你的办公 IP 开放 22/3306/6379**，或 3306/6379 只监听内网、微服务与数据库同机。
- 已在聊天中暴露的 root 密码应 **尽快修改**，并改用 SSH 密钥登录。

## 2. 服务器上部署基础设施

### 2.0 已在网页终端 / SSH 里登录服务器（最快）

**先把本仓库推到 GitHub**（含 `deploy/docker-compose.server.yml` 等），然后在服务器执行：

```bash
curl -fsSL https://raw.githubusercontent.com/dxx01/ai-study/main/deploy/bootstrap-on-server.sh | bash
```

或克隆后执行：

```bash
git clone --depth 1 https://github.com/dxx01/ai-study.git /opt/ai-study
bash /opt/ai-study/deploy/bootstrap-on-server.sh
```

> Cursor 所在环境无法替你输入 SSH 密码；你在本机 PowerShell 可用 `.\scripts\deploy-aliyun-infra.ps1 -ServerHost 47.120.50.85`（交互输入密码）。

### 2.1 安装 Docker（Ubuntu 22/24 示例）

```bash
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
docker compose version
```

### 2.2 上传 deploy 目录

将仓库中 `deploy/`（含 `sql/`、`docker-compose.server.yml`）放到服务器，例如 `/opt/ai-study/deploy`，并上传 `env/server.env`。

### 2.3 配置并启动

```bash
cd /opt/ai-study/deploy
# 上传或编辑 env/server.env（应用 + Docker 共用）
docker compose --env-file env/server.env -f docker-compose.server.yml up -d
docker compose --env-file env/server.env -f docker-compose.server.yml ps
```

空数据卷首次启动会自动执行 `01-init-database.sql` → `02-schema.sql` → `03-seed-data.sql`（演示账号 admin/admin123）。

应用与 Docker 均以 gitignored 的 `env/server.env` 为准；库名 `ai_platform`。

### 2.4 已有库增量

若从旧版升级，在服务器上按 [deploy/sql/README.md](../../deploy/sql/README.md) 执行 `07`、`08` 等增量脚本。

## 3. 本机连服务器跑微服务

```powershell
# 确认 deploy\env\server.env 存在（从 example 复制并填写），并 use-env.ps1 -Target server
.\scripts\switch-profile.ps1 -Target server
.\scripts\use-jdk17.ps1
# 再按 README 启动各 backend 模块

# 平时切回本地
.\scripts\switch-profile.ps1 -Target local
```

`switch-profile.ps1` 会清掉可能残留的 `MYSQL_*` / `REDIS_*` 环境变量，只设置 `SPRING_PROFILES_ACTIVE=server|dev`；具体连接参数分别由 `application-server.yml` / `application-dev.yml` 导入对应 env 文件。

各服务读 `current.env`（`use-env.ps1` 从 `local.env` / `server.env` 同步），并注册 Nacos。IDEA 直接 Run，勿在 Run Configuration 写 `MYSQL_*` / `REDIS_*`。

## 4. 与本地 Docker 的差异

| 项 | 本地 `docker-compose.yml` | 服务器 `docker-compose.server.yml` |
|----|---------------------------|-------------------------------------|
| MySQL 应用密码 | ai_dev_password | 见 env 配置 |
| Redis 密码 | 无 | 见 env 配置 |
| 初始化 | 01+01b+02+03 | 01+02+03（用户由 MYSQL_USER 创建） |

## 5. 一键远程部署（可选）

在 **本机** 仓库根目录（需已配置 SSH 免密或交互输入 root 密码）：

```powershell
.\scripts\deploy-aliyun-infra.ps1 -ServerHost 47.120.50.85 -RemoteDir /opt/ai-study/deploy
```

## 6. Nacos（服务发现 + 配置中心）

- 容器：`ai-study-nacos`，Nacos 2.4.3，MySQL 存储库 `nacos_config`
- 端口：`8848`（HTTP/Console）、`9848`（gRPC）
- 服务器目录：`/opt/ai-study/deploy/nacos`
- 配置 Data ID：`application-common.yaml`、`ai-auth.yaml`、`ai-gateway.yaml`、
  `ai-service-llm.yaml`、`ai-service-knowledge.yaml`、`ai-service-conversation.yaml`
- 详细操作见 [deploy/nacos/README.md](../../deploy/nacos/README.md)

本机访问（安全组未放行 8848/9848 时）：

```powershell
.\scripts\start-nacos-tunnel.ps1
# 另开窗口
.\scripts\switch-profile.ps1 -Target server
```

所有后端服务在 `server` profile 下会从 Nacos 读取配置，并注册到 Nacos；网关使用
`lb://ai-auth`、`lb://ai-service-llm` 等路由。本地 `dev` profile 仍使用固定 localhost 地址，不依赖 Nacos。

> Nacos 控制台账号见 `deploy/env/server.env` 的 `NACOS_USERNAME` / `NACOS_PASSWORD`（勿提交 Git）；Docker 鉴权 token 在同文件底部。

