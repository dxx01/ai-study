# 本地 MySQL 开发

适用于：**本机安装的 MySQL Server**（非 Docker 部署场景）。

## 连接参数

| 项 | 值 |
|----|-----|
| 主机 | `127.0.0.1` |
| 端口 | `3306` |
| 库名 | `ai_platform` |
| 管理账号 | `root`（密码见 `deploy/env/local-mysql.env`，已 gitignore） |
| 应用账号 | `ai` / `ai_dev_password`（`init-mysql-local.ps1` 创建，微服务默认使用） |

JDBC（与 `application-dev.yml` 一致，时区 **Asia/Shanghai**）：

```text
jdbc:mysql://127.0.0.1:3306/ai_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
```

## 环境变量

root 账号只用于初始化与命令行；`local-mysql.env` 已被 gitignore：

```powershell
Copy-Item deploy\env\local-mysql.env.example deploy\env\local-mysql.env
# 编辑 deploy\env\local-mysql.env，填写 MYSQL_PASSWORD
.\scripts\load-local-mysql-env.ps1
```

初始化脚本会执行 `01b` 创建 `ai` 应用账号并授权，因此微服务不设置 `MYSQL_*` 也能用默认值连接。

## 初始化流程

一条命令建库、建表、种子数据：

```powershell
.\scripts\init-mysql-local.ps1
```

等价执行 `deploy/sql/01-init-database.sql` → `02-schema.sql` → `03-seed-data.sql`。

**验证：**

```powershell
$env:MYSQL_PWD = $env:MYSQL_PASSWORD
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --host=127.0.0.1 -uroot -e "USE ai_platform; SHOW TABLES; SELECT id, username FROM sys_user;"
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

本机密码只保存在 `deploy/env/local-mysql.env`（已 gitignore），**不要**写回文档、脚本或任何会被提交的文件；生产环境必须使用独立强密码与密钥托管。
