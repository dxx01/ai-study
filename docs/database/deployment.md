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

微服务环境变量模板：[deploy/env/deploy-mysql.env.example](../../deploy/env/deploy-mysql.env.example)。

## 初始化流程

### 1. 启动基础设施

```powershell
cd D:\work\idea\ai-study
docker compose -f deploy/docker-compose.yml up -d
```

Compose 在 **数据卷为空** 时按文件名顺序自动执行：

- `deploy/sql/01-init-database.sql` — 建库
- `deploy/sql/01b-grant-docker-user.sql` — 创建 `ai` 用户
- `deploy/sql/02-schema.sql` — 业务表 DDL
- `deploy/sql/03-seed-data.sql` — 演示账号

> 已有数据卷不会再执行初始化脚本。存量库请手工执行 `02`、`03`；若应用账号 `ai` 不存在，再单独执行 `01b-grant-docker-user.sql`。
> 也可运行 `.\scripts\init-mysql-local.ps1 -User root -Password root_dev` 只补建表与种子（本地脚本不再创建 `ai`）。

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
| DB 账号 | root（来自 `local-mysql.env`） | ai / ai_dev_password |
| 初始化 | `init-mysql-local.ps1`（01+02+03） | 01+01b + 02+03 |

逻辑表结构见 [schema.md](schema.md)。
