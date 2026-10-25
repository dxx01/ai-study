# 数据安全规范

## 1. 密码与密钥

- 用户密码：**BCrypt** 存储于 `password_hash`；禁止明文、禁止 MD5/SHA 单次哈希  
- API Key、DB 密码：**环境变量** 或 gitignore 的本地 env 文件；禁止硬编码、禁止入 Git  
- 日志禁止输出密码、token 全文、API Key  

### 1.1 模型 / 检索密钥边界（本项目）

- **模型 API Key 只允许出现在 `ai-service-llm`**；`ai-auth`、`ai-service-conversation`、`ai-service-knowledge`、`ai-gateway`、`frontend` 不得读取或转发。  
- 联网检索 Key（`TAVILY_API_KEY`）同上，仅 `ai-service-llm`。  
- 若 embedding 改为外部 API，使用**独立**的 embedding Key，只允许出现在 `ai-service-knowledge`，不得与模型 Key 共用。  
- 网关是公网入口，**不持有**任何上游 AI Key。  
- 生产用环境变量 / K8s Secret / Vault；本地用 gitignored 的 `.env.*.properties`；禁止打入镜像或提交仓库。  
- 启动校验只判断“是否配置 / 是否为占位符”，**不打印** Key 内容。  

## 2. 鉴权与隔离

- 数据查询必须在 **后端** 校验 Sa-Token 登录态与权限码  
- 会话/消息：必须校验 **owner_user_id** 与当前 `StpUtil.getLoginIdAsLong()` 一致  
- 禁止根据客户端任意传入的 userId 查询他人数据  

## 3. SQL 注入

- 禁止拼接用户输入成 SQL  
- Mapper SQL 使用 `#{}` 占位符；动态条件用 MyBatis-Plus `Wrapper` 或受控 XML，禁止拼接用户输入  

## 4. 日志脱敏

- 手机号、身份证、完整消息正文（生产 info 级）按策略脱敏  
- 异常日志带上下文，但不带密钥  

## 5. 本地与生产

- 本地 root 密码见 [local-development.md](../database/local-development.md)，**不得**用于生产  
- 部署账号见 [deployment.md](../database/deployment.md)，生产须轮换  

## 6. 保留与删除

- 会话 CASCADE 删除消息为当前产品行为；变更须评审  
- 后续合规删除/归档策略在需求明确后补充  
