package com.example.ai.common.api;

/**
 * 对话模式：本地知识库 / 联网 / 自动选择。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
public enum ChatMode {
    /** 仅使用大模型 + 本地向量库（RAG） */
    LOCAL,
    /** 在回答前附加联网检索摘要（需配置 Tavily） */
    WEB,
    /** 根据问题意图决定是否联网；始终启用本地 RAG */
    AUTO
}
