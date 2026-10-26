# 本地 MySQL 开发

适用于：**本机安装的 MySQL Server**（非 Docker 部署场景）。

## 连接参数

| 项 | 值 |
|----|-----|
| 主机 | `127.0.0.1` |
| 端口 | `3306` |
| 库名 | `ai_platform` |
| 账号 | `root`（密码见 `deploy/env/local.env`，已 gitignore；服务与初始化共用） |

JDBC（与 `application-dev.yml` 一致，时区 **Asia/Shanghai**）：

```text
jdbc:mysql://127.0.0.1:3306/ai_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
```

## 环境变量

本机账号来自 `deploy/env/local.env`（已 gitignore）：

```powershell
# 编辑 deploy\env\local.env（若无则新建），填写 MYSQL_PASSWORD
.\scripts\use-env.ps1 -Target local
.\scripts\load-local-env.ps1   # 仅初始化 SQL 脚本需要
```

微服务通过 `current.env`（由 `use-env.ps1` 从 `local.env` 同步）自动加载，**启动服务不需要手动 export**。

## 初始化流程

一条命令建库、建表、种子数据：

```powershell
.\scripts\init-mysql-local.ps1
```

等价执行 `deploy/sql/01-init-database.sql` → `02-schema.sql` → `03-seed-data.sql`。

> 新库直接用 `02-schema.sql`（v0.7）。已有库依次执行 `04-schema-v2.sql`、`05-rbac.sql`、`06-fix-comments.sql`、`07-platform-nav.sql`、`08-enable-status-tinyint.sql`，再执行一次 `03-seed-data.sql`（幂等）。

**验证：**

```powershell
$env:MYSQL_PWD = $env:MYSQL_PASSWORD
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --default-character-set=utf8mb4 --host=127.0.0.1 -uroot -e "USE ai_platform; SHOW TABLES; SELECT id, username, status FROM sys_user;"
```

## 重建库（旧结构）

```powershell
.\scripts\init-mysql-local.ps1 -Recreate
```

## 结构变更

同步修改 `deploy/sql/02-schema.sql`、MyBatis-Plus 实体、[schema.md](schema.md)；已存在库需手工执行增量 SQL。

## 命令行客户端

```powershell
$env:MYSQL_PWD = $env:MYSQL_PASSWORD
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --host=127.0.0.1 -uroot ai_platform
```

## 安全说明

本机密码只保存在 `deploy/env/local.env`（已 gitignore），**不要**写回文档、脚本或任何会被提交的文件；生产环境必须使用独立强密码与密钥托管。

