package com.example.ai.langchain4j.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 加载 classpath 下 application*.yml 及模块根目录 .env.{profile}（非 Spring 环境下的简化实现）。
 */
public final class AppConfigLoader {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}:]+)(?::([^}]*))?}");

    private static final String KEY_API_KEY = "spring.ai.openai.api-key";
    private static final String KEY_BASE_URL = "spring.ai.openai.base-url";
    private static final String KEY_MODEL = "spring.ai.openai.chat.options.model";
    private static final String KEY_TEMPERATURE = "spring.ai.openai.chat.options.temperature";

    private AppConfigLoader() {
    }

    public static AppSettings load() {
        Map<String, String> values = new LinkedHashMap<>();

        mergeYaml(values, loadYamlResource("application.yml"));
        String profile = firstNonBlank(
                System.getenv("SPRING_PROFILES_ACTIVE"),
                values.get("spring.profiles.active"),
                "dev");
        values.put("spring.profiles.active", profile);

        mergeYaml(values, loadYamlResource("application-" + profile + ".yml"));
        Path base = Path.of(System.getProperty("user.dir"));
        mergeDotEnv(values, base.resolve(".env." + profile));
        mergeDotEnv(values, base.resolve(".env." + profile + ".properties"));
        mergeDotEnv(values, base.resolve("../ai-service-llm/.env." + profile + ".properties"));

        String apiKey = resolve(KEY_API_KEY, values);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "未配置 OPENAI_API_KEY：请在 .env." + profile + " 中设置，或导出环境变量。");
        }

        String baseUrl = resolve(KEY_BASE_URL, values);
        String model = resolve(KEY_MODEL, values);
        double temperature = Double.parseDouble(resolve(KEY_TEMPERATURE, values));

        return new AppSettings(profile, apiKey, baseUrl, model, temperature);
    }

    private static void mergeDotEnv(Map<String, String> target, Path path) {
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String val = trimmed.substring(eq + 1).trim();
                putIfAbsentEnv(target, key, val);
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取 " + path + " 失败", e);
        }
    }

    private static void putIfAbsentEnv(Map<String, String> target, String envKey, String fileValue) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            target.put(envKey, env);
        } else {
            target.put(envKey, fileValue);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadYamlResource(String name) {
        InputStream in = AppConfigLoader.class.getClassLoader().getResourceAsStream(name);
        if (in == null) {
            return Map.of();
        }
        try (in) {
            Object loaded = new Yaml().load(in);
            if (loaded instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
            return Map.of();
        } catch (IOException e) {
            throw new IllegalStateException("读取配置 " + name + " 失败", e);
        }
    }

    private static void mergeYaml(Map<String, String> flat, Map<String, Object> yaml) {
        flatten("", yaml, flat);
    }

    @SuppressWarnings("unchecked")
    private static void flatten(String prefix, Map<String, Object> source, Map<String, String> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                flatten(key, (Map<String, Object>) nested, target);
            } else if (value != null) {
                target.put(key, value.toString());
            }
        }
    }

    private static String resolve(String dottedKey, Map<String, String> values) {
        String raw = values.get(dottedKey);
        if (raw == null) {
            return null;
        }
        return resolvePlaceholders(raw, values);
    }

    private static String resolvePlaceholders(String raw, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER.matcher(raw);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);
            String defaultValue = matcher.group(2);
            String replacement = firstNonBlank(System.getenv(name), values.get(name), defaultValue);
            if (replacement == null) {
                replacement = "";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String firstNonBlank(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.isBlank()) {
                return c;
            }
        }
        return null;
    }

    public record AppSettings(String profile, String apiKey, String baseUrl, String model, double temperature) {
    }
}
