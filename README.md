# AI 应用平台

Spring Cloud 微服务 + Spring AI + Vue 3。先读 [项目说明书](docs/project-charter.md) 明确范围与验收标准，协作约定见 [AGENTS.md](AGENTS.md)。

## 目录

```
backend/          # ai-gateway, ai-auth, ai-service-*
frontend/
deploy/           # Docker Compose、deploy/sql（建库 → 授权 → 建表 → 种子）
docs/
  database/       # schema / 本地 / 部署（分文档）
  standards/      # 数据库与数据访问规范
```

## 要求

JDK **17+**（`.\scripts\use-jdk17.ps1`）、Maven 3.9+、MySQL 8、Redis、Node **22+**（建议 24 LTS，见 `frontend/.nvmrc`）。Node 未安装时可用 `nvm install 24` 或 `winget install OpenJS.NodeJS.LTS`。

## 数据库（请先读）

| 场景 | 文档 |
|------|------|
| 表结构 | [docs/database/schema.md](docs/database/schema.md) |
| 本机 MySQL | [docs/database/local-development.md](docs/database/local-development.md) |
| Docker 部署 | [docs/database/deployment.md](docs/database/deployment.md) |

表结构以 `deploy/sql/02-schema.sql` 为准（手工 DDL，前期不用 Flyway），改表时同步实体与 `schema.md`。

## 本地 / 服务器配置切换

两套配置互不影响，平时开发默认本地：

```powershell
# 本地 MySQL + Redis（默认 dev）
.\scripts\switch-profile.ps1 -Target local

# 服务器 47.120.50.85 的 MySQL + Redis（server profile）
.\scripts\switch-profile.ps1 -Target server
```

维护 `deploy/env/local.env` 或 `server.env`（仅 MySQL/Redis 不同），执行 `.\scripts\use-env.ps1 -Target local|server` 生成 `current.env`；各服务启动读 `current.env` 并注册 Nacos。另开窗口 `.\scripts\start-nacos-tunnel.ps1`（未放行公网 8848 时）。**IDEA 不必设 Active profiles**。

服务器 `server` profile 还依赖 Nacos（服务发现 + 配置中心）。安全组未放行 8848/9848 时，先在单独窗口运行：

```powershell
.\scripts\start-nacos-tunnel.ps1
```

再切到 server。Nacos 控制台（隧道）：`http://127.0.0.1:8848/nacos/`。

## 启动基础设施（二选一）

### A. 本机 MySQL（无需 Docker，当前机器可用）

首次先把密码写进 gitignored 的 env 文件：

```powershell
# 编辑 deploy\env\local.env，填写 MYSQL_PASSWORD
.\scripts\use-env.ps1 -Target local
.\scripts\load-local-env.ps1
.\scripts\init-mysql-local.ps1          # 建库 + 表 + 种子
```

微服务从 `deploy/env/current.env` 读取连接信息（由 `use-env.ps1` 从 `local.env` 同步），**不需要**手动 export。

### B. Docker（可选）

```powershell
.\scripts\dev-up.ps1
```

MySQL 3306 + Redis 6379。**空数据卷首次启动**会自动执行 `01 → 01b → 02 → 03`（建库、应用账号、业务表、演示账号）。

存量数据卷不会重复初始化；需要重建时执行 `docker compose -f deploy/docker-compose.yml down -v` 再 `up`。Docker MySQL 账号 `ai / ai_dev_password` 写入 `local.env` 即可。

## 启动服务

首次先把模块安装到本地仓库（`-pl` 单模块启动依赖它）：

```powershell
.\scripts\use-jdk17.ps1
mvn -q -DskipTests install
```

按顺序各开一个终端：

```powershell
mvn -pl backend/ai-auth spring-boot:run
mvn -pl backend/ai-service-knowledge spring-boot:run
mvn -pl backend/ai-service-conversation spring-boot:run
mvn -pl backend/ai-service-llm spring-boot:run
mvn -pl backend/ai-gateway spring-boot:run
```

> 知识库默认首次启动会从 GitHub 下载 `all-MiniLM-L6-v2` ONNX 模型（约 90MB），缓存到 `backend/ai-service-knowledge/data/model-cache`；网络受限会卡住。
> 可先把 `model.onnx` / `tokenizer.json` 放到 `data/model-cache/all-MiniLM-L6-v2/`，运行 `.\scripts\use-local-embedding-model.ps1` 生成本地配置；
> 之后正常启动会自动使用本地模型，**不需要加任何参数**。详见 [docs/microservices.md](docs/microservices.md)。

前端：

```powershell
cd frontend
npm install        # 首次，需要 Node 18+
npm run dev        # http://localhost:5173
```

LLM 密钥放在 `backend/ai-service-llm/.env.dev.properties`（已被 gitignore）。演示账号 `admin/admin123`、`user/user123`。

## 四步冒烟

1. 打开 http://localhost:5173，用 `admin/admin123` 登录
2. 发送“你好”，应返回回答与免责声明
3. 知识库页上传 [示例文档](docs/samples/hypertension-diet.md)（或任意 PDF/Word），列表出现文档 ID 与分块数
4. 提问“高血压人群每天食盐摄入建议不超过多少克？”，回答应包含“5 克”并带“参考依据”

任何一步失败，先修这一步，再继续。

## 构建与测试

```powershell
mvn clean verify
```

受限环境（沙箱/部分 CI）里 Mockito 无法外部 attach，可加 `-DargLine="-Djdk.attach.allowAttachSelf=true"`；普通 IDE 与 CI 不需要。

更多：[项目说明书](docs/project-charter.md)、[学习日志](docs/learning-log.md)、[docs/microservices.md](docs/microservices.md)、[docs/roadmap.md](docs/roadmap.md)。
