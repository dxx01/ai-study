package com.example.ai.llm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM 服务配置（专家画像、联网、下游服务地址）。
 *
 * @author 邓鑫鑫
 * @since 0.3.0
 */
@ConfigurationProperties(prefix = "app")
public class LlmProperties {

    private Expert expert = new Expert();
    private Ai ai = new Ai();
    private WebSearch webSearch = new WebSearch();
    private RateLimit rateLimit = new RateLimit();
    private Services services = new Services();

    public Expert getExpert() {
        return expert;
    }

    public void setExpert(Expert expert) {
        this.expert = expert;
    }

    public Ai getAi() {
        return ai;
    }

    public void setAi(Ai ai) {
        this.ai = ai;
    }

    public WebSearch getWebSearch() {
        return webSearch;
    }

    public void setWebSearch(WebSearch webSearch) {
        this.webSearch = webSearch;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    public static class Ai {
        private String defaultProvider = "deepseek";
        private Map<String, Provider> providers = new LinkedHashMap<>();

        public String getDefaultProvider() {
            return defaultProvider;
        }

        public void setDefaultProvider(String defaultProvider) {
            this.defaultProvider = defaultProvider;
        }

        public Map<String, Provider> getProviders() {
            return providers;
        }

        public void setProviders(Map<String, Provider> providers) {
            this.providers = providers;
        }

        public static class Provider {
            private String baseUrl;
            private String apiKey;
            private String model;
            private Double temperature = 0.7;
            private Integer maxTokens = 2048;

            public String getBaseUrl() {
                return baseUrl;
            }

            public void setBaseUrl(String baseUrl) {
                this.baseUrl = baseUrl;
            }

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }

            public String getModel() {
                return model;
            }

            public void setModel(String model) {
                this.model = model;
            }

            public Double getTemperature() {
                return temperature;
            }

            public void setTemperature(Double temperature) {
                this.temperature = temperature;
            }

            public Integer getMaxTokens() {
                return maxTokens;
            }

            public void setMaxTokens(Integer maxTokens) {
                this.maxTokens = maxTokens;
            }
        }
    }

    public static class Expert {
        private String id = "home-health-advisor";
        private String displayName = "居家营养与健康科普顾问";
        private String tagline = "";
        private List<String> domains = new ArrayList<>();
        private List<String> principles = new ArrayList<>();
        private String disclaimer = "";

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getTagline() {
            return tagline;
        }

        public void setTagline(String tagline) {
            this.tagline = tagline;
        }

        public List<String> getDomains() {
            return domains;
        }

        public void setDomains(List<String> domains) {
            this.domains = domains;
        }

        public List<String> getPrinciples() {
            return principles;
        }

        public void setPrinciples(List<String> principles) {
            this.principles = principles;
        }

        public String getDisclaimer() {
            return disclaimer;
        }

        public void setDisclaimer(String disclaimer) {
            this.disclaimer = disclaimer;
        }
    }

    public static class WebSearch {
        private boolean enabled;
        private String tavilyApiKey = "";
        private int maxResults = 5;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTavilyApiKey() {
            return tavilyApiKey;
        }

        public void setTavilyApiKey(String tavilyApiKey) {
            this.tavilyApiKey = tavilyApiKey;
        }

        public int getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(int maxResults) {
            this.maxResults = maxResults;
        }

        public boolean isConfigured() {
            return enabled && tavilyApiKey != null && !tavilyApiKey.isBlank();
        }
    }

    public static class RateLimit {
        private boolean enabled = true;
        private int chatPerMinute = 20;
        private int chatPerDay = 200;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getChatPerMinute() {
            return chatPerMinute;
        }

        public void setChatPerMinute(int chatPerMinute) {
            this.chatPerMinute = chatPerMinute;
        }

        public int getChatPerDay() {
            return chatPerDay;
        }

        public void setChatPerDay(int chatPerDay) {
            this.chatPerDay = chatPerDay;
        }
    }

    public static class Services {
        private String knowledgeBaseUrl = "http://127.0.0.1:9202";
        private String conversationBaseUrl = "http://127.0.0.1:9203";

        public String getKnowledgeBaseUrl() {
            return knowledgeBaseUrl;
        }

        public void setKnowledgeBaseUrl(String knowledgeBaseUrl) {
            this.knowledgeBaseUrl = knowledgeBaseUrl;
        }

        public String getConversationBaseUrl() {
            return conversationBaseUrl;
        }

        public void setConversationBaseUrl(String conversationBaseUrl) {
            this.conversationBaseUrl = conversationBaseUrl;
        }
    }
}
