# frontend

Vue 3 + Element Plus + Pinia。

- 登录 / 注册 → `/auth/**`
- 对话 → `/api/chat`
- 知识库 → `/api/knowledge/**`

```powershell
cd D:\work\idea\ai-study\frontend
npm install
npm run dev
```

Node 22+（建议 24 LTS，见 `.nvmrc`）。若内网 npm registry 不可达，可改用镜像：

```powershell
npm install --registry=https://registry.npmmirror.com
```

开发时代理到网关 `http://127.0.0.1:8080`（见 `vite.config.ts`）。
