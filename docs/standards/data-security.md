# 数据安全规范

## 1. 密码与密钥

- 用户密码：**BCrypt** 存储于 `password_hash`；禁止明文、禁止 MD5/SHA 单次哈希  
- API Key、DB 密码：**环境变量** 或 gitignore 的本地 env 文件；禁止硬编码、禁止入 Git  
- 日志禁止输出密码、token 全文、API Key  

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
