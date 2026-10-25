package com.example.ai.llm.expert;

import com.example.ai.llm.config.LlmProperties.Expert;

/**
 * 专家 system 指令组装。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
public final class ExpertSystemPrompt {

    private ExpertSystemPrompt() {
    }

    /**
     * @param expert 专家配置
     * @return system prompt
     */
    public static String build(Expert expert) {
        StringBuilder sb = new StringBuilder();
        sb.append("【角色】").append(expert.getDisplayName()).append("（ID: ").append(expert.getId()).append("）\n");
        if (expert.getTagline() != null && !expert.getTagline().isBlank()) {
            sb.append("【定位】").append(expert.getTagline()).append("\n");
        }
        if (!expert.getDomains().isEmpty()) {
            sb.append("【擅长领域】\n");
            for (String domain : expert.getDomains()) {
                sb.append("- ").append(domain).append("\n");
            }
        }
        if (!expert.getPrinciples().isEmpty()) {
            sb.append("【工作原则】\n");
            for (String principle : expert.getPrinciples()) {
                sb.append("- ").append(principle).append("\n");
            }
        }
        sb.append("【输出要求】\n");
        sb.append("- 使用简体中文，结构清晰（必要时用小标题与列表）\n");
        sb.append("- 若引用了本地知识片段，在回答末尾列出「参考依据」并标注文档名\n");
        sb.append("- 若使用了联网摘要，在「参考依据」中标注「联网检索」及来源标题\n");
        if (expert.getDisclaimer() != null && !expert.getDisclaimer().isBlank()) {
            sb.append("【免责声明】").append(expert.getDisclaimer()).append("\n");
        }
        return sb.toString();
    }
}
