# deploy/env

| 文件 | Git | 说明 |
|------|-----|------|
| `local.env` | ignore | 本机 MySQL/Redis；Nacos 指向云 `47.120.50.85:8848` |
| `server.env` | ignore | 云 MySQL/Redis/Nacos；**同文件**供服务器 `docker-compose.server.yml` |
| `current.env` | ignore | `use-env.ps1` 从 local/server 复制，微服务只读此文件 |

```powershell
.\scripts\use-env.ps1 -Target local   # 或 server
.\scripts\check-nacos.ps1
.\scripts\publish-nacos-configs.ps1   # 把仓库 configs 推到远端 Nacos
```

服务器 Compose：`docker compose --env-file env/server.env -f docker-compose.server.yml up -d`

## Redis 连不上 `47.120.50.85:6379`

说明当前是 **`server` 配置**（`current.env` 里 `REDIS_HOST=47.120.50.85`），但本机访问 **6379 超时**（常见：安全组只放了 8848，没放 Redis）。

- **本机开发、库也在本机**：`.\scripts\use-env.ps1 local`，并启动本机 Redis（Docker 或 `deploy/docker-compose.yml`）。
- **本机连云 MySQL、Redis 在云上**：安全组放行 **6379**，或运行 `.\scripts\start-cloud-tunnel.ps1`，`server.env` 改为 `REDIS_HOST=127.0.0.1` 后重新 `use-env.ps1 server`。
