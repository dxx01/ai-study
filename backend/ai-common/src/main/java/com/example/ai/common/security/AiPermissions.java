package com.example.ai.common.security;

/**
 * 单机微服务统一的 Sa-Token 权限码（auth 与 expert 服务共用）。
 *
 * @author 邓鑫鑫
 * @since 0.2.0
 */
public final class AiPermissions {

    private AiPermissions() {
    }

    /** 对话与 GET 试用 */
    public static final String CHAT = "ai:chat";

    /** 知识库列表 */
    public static final String KNOWLEDGE_READ = "ai:knowledge:read";

    /** 上传文档 */
    public static final String KNOWLEDGE_WRITE = "ai:knowledge:write";

    /** 重建索引 */
    public static final String KNOWLEDGE_ADMIN = "ai:knowledge:admin";

    /** 用户管理 */
    public static final String SYS_USER_MANAGE = "sys:user:manage";

    /** 角色管理 */
    public static final String SYS_ROLE_MANAGE = "sys:role:manage";

    /** 菜单管理 */
    public static final String SYS_MENU_MANAGE = "sys:menu:manage";
}
