# 部署环境 MySQL

适用于：**Docker Compose** 或目标环境使用应用账号 `ai` 的场景。

## 连接参数（Compose 默认）

| 项 | 值 |
|----|-----|
| 主机 | `127.0.0.1`（端口映射 3306） |
| 库名 | `ai_platform` |
| 应用账号 | `ai` |
| 应用密码 | `ai_dev_password` |
| root（容器内） | `root_dev` |

本机 Docker MySQL 账号见 `deploy/docker-compose.yml`（`ai` / `ai_dev_password`），连库时写入 `deploy/env/local.env`。

## 初始化流程

### 1. 启动基础设施

```powershell
cd D:\work\idea\ai-study
docker compose -f deploy/docker-compose.yml up -d
```

Compose 在 **数据卷为空** 时按文件名顺序自动执行：

- `deploy/sql/01-init-database.sql` — 建库
- `deploy/sql/01b-grant-docker-user.sql` — 创建 `ai` 用户
- `deploy/sql/02-schema.sql` — v0.7 全量 DDL
- `deploy/sql/03-seed-data.sql` — 演示账号 + 角色/权限/菜单

> 已有数据卷不会再执行初始化脚本。存量旧库依次执行 `04-schema-v2.sql`、`05-rbac.sql`、`06-fix-comments.sql`、`07-platform-nav.sql`、`08-enable-status-tinyint.sql`，再执行一次 `03-seed-data.sql`；
> 若应用账号 `ai` 不存在，执行 `01b-grant-docker-user.sql`。

### 2. 推荐启动顺序（完整栈）

1. `deploy/docker-compose.yml`（MySQL + Redis）  
2. （空卷自动完成；存量库补执行 DDL + 种子）  
3. `backend/ai-auth`  
4. `backend/ai-service-conversation`  
5. `backend/ai-service-knowledge`、`backend/ai-service-llm`  
6. `backend/ai-gateway`  
7. `frontend`

使用部署账号：

```powershell
$env:MYSQL_USER = "ai"
$env:MYSQL_PASSWORD = "ai_dev_password"
```

## 阿里云服务器（47.120.50.85）

服务器已用 Docker 部署 MySQL 8.4 + Redis 7，目录 `/opt/ai-study/deploy`。

### 启动 / 停止

```bash
cd /opt/ai-study/deploy

# 启动（必须带 --env-file，密码才会参与 compose 变量替换）
docker compose --env-file env/server.env -f docker-compose.server.yml up -d

# 状态 / 日志
docker compose --env-file env/server.env -f docker-compose.server.yml ps
docker logs -f ai-study-mysql
docker logs -f ai-study-redis

# 停止（保留数据卷）
docker compose --env-file env/server.env -f docker-compose.server.yml down
```

连接参数：

| 项 | 值 |
|----|-----|
| MySQL | `47.120.50.85:3306`，库 `ai_platform`，应用账号 `ai` |
| MySQL root | 仅容器内/本机使用 |
| Redis | `47.120.50.85:6379` |
| 密码 | 见 `deploy/env/server.env`（gitignore） |

### 应用切到服务器 profile

执行 `.\scripts\use-env.ps1 -Target server` 后，微服务读 `deploy/env/current.env`（来自 `server.env`）。本机 PowerShell：

```powershell
# 切到服务器
.\scripts\switch-profile.ps1 -Target server

# 平时切回本地
.\scripts\switch-profile.ps1 -Target local
```

脚本会清掉残留的 `MYSQL_*` / `REDIS_*` 环境变量，只切换 `SPRING_PROFILES_ACTIVE`，避免“切回本地却还连服务器”。

或在服务器 shell 中：

```bash
set -a
source deploy/env/server.env
set +a
java -jar backend/ai-auth/target/ai-auth-0.1.0-SNAPSHOT.jar
```

默认仍是 `dev`；本地开发不受影响。

### 安全要求

- 当前 3306/6379 映射到公网，密码是演示级弱密码；**必须**在阿里云安全组只放行自己的出口 IP，或改成内网/SSH 隧道。
- 如果应用和数据库同机部署，建议把 `docker-compose.server.yml` 的端口改为 `127.0.0.1:3306:3306`、`127.0.0.1:6379:6379`。
- 生产环境不要把 root 密码写进任何提交文件；`deploy/env/server.*.env` 已被 `.gitignore` 忽略。
- 定期备份：`docker exec ai-study-mysql sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" ai_platform' > backup.sql`。

## 数据卷与重建

- 数据持久化在 Docker volume `ai_platform_mysql_data`。  
- **清空数据**：`docker compose -f deploy/docker-compose.yml down -v` 后重新 `up`，初始化脚本会自动重建全部表与种子。  
- 生产环境禁止随意 `-v`；需备份与变更评审。

## 生产差异（待环境确认）

| 项 | 建议 |
|----|------|
| 密码 | 独立强密码，密钥托管 |
| 账号 | 最小权限，仅 `ai_platform` |
| DDL | 发布流水线执行审核后的 SQL，禁止应用启动时隐式改表 |
| 备份 | 定期全量 + binlog |

## 与本地开发的区别

| 维度 | 本地开发 | 部署（Compose） |
|------|----------|-----------------|
| 文档 | [local-development.md](local-development.md) | 本文 |
| DB 账号 | root（来自 `local.env`） | ai / ai_dev_password |
| 初始化 | `init-mysql-local.ps1`（01+02+03） | 01+01b + 02+03 |

逻辑表结构见 [schema.md](schema.md)。


