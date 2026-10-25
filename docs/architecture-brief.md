# 架构一页纸

## 调用链

```
frontend → ai-gateway (8080)
  → ai-auth                 sys_user（MySQL）
  → ai-service-llm          编排 → conversation + knowledge + 模型
  → ai-service-knowledge    RAG / 文件
  → ai-service-conversation chat_session / chat_message（MySQL）
Redis：Sa-Token Session
MySQL：库 ai_platform（本地与部署连接方式见 docs/database/）
```

## 数据

- 结构唯一来源：`deploy/sql/02-schema.sql` + [schema.md](database/schema.md)（手工 DDL，前期不用 Flyway）
- 规范：[standards/database-design.md](standards/database-design.md)  
- `owner_user_id` BIGINT 关联 `sys_user.id`；跨服务无 FK  

## 面试可讲点

- 微服务拆分 + HTTP 编排 + Bearer 透传  
- JSON 权限 + Sa-Token Session；可演进 RBAC 表  
- 消息排序 `created_at, id`；会话消息持久化  

详见 [microservices.md](microservices.md)、[database/schema.md](database/schema.md)。
