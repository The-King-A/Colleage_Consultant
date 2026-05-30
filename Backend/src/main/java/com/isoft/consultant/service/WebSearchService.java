package com.isoft.consultant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class WebSearchService {

    private static final Logger log = LoggerFactory.getLogger(WebSearchService.class);

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public WebSearchService(
            @Value("${tavily.base-url}") String baseUrl,
            @Value("${tavily.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @SuppressWarnings("unchecked")
    public String search(String userQuery) {
        String searchQuery = buildSearchQuery(userQuery);

        try {
            log.info("联网搜索: query={}", searchQuery);

            Map<String, Object> body = Map.of(
                    "api_key", apiKey,
                    "query", searchQuery,
                    "search_depth", "advanced",
                    "max_results", 5,
                    "include_answer", true,
                    "include_domains", List.of(),
                    "exclude_domains", List.of()
            );

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/search"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("联网搜索失败: HTTP {} — {}", response.statusCode(), response.body());
                return buildEmptyResult();
            }

            Map<String, Object> respBody = objectMapper.readValue(response.body(), Map.class);

            StringBuilder sb = new StringBuilder();
            sb.append("\n\n");
            sb.append("══════════════════════════════════════\n");
            sb.append("【重要指令】以下信息来自互联网实时检索，你必须严格基于这些信息回答用户问题。\n");
            sb.append("如果搜索结果中有具体数据（如分数线、日期、政策），请直接引用并注明来源。\n");
            sb.append("如果搜索结果不足以回答用户问题，请如实告知，禁止编造数据。\n");
            sb.append("══════════════════════════════════════\n\n");

            String answer = (String) respBody.get("answer");
            if (answer != null && !answer.isBlank()) {
                sb.append("📝 搜索结果摘要：\n").append(answer).append("\n\n");
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) respBody.get("results");
            if (results != null && !results.isEmpty()) {
                int count = 0;
                for (Map<String, Object> r : results) {
                    count++;
                    String title = (String) r.getOrDefault("title", "");
                    String url = (String) r.getOrDefault("url", "");
                    String content = (String) r.getOrDefault("content", "");
                    if (content.length() > 800) {
                        content = content.substring(0, 800) + "...";
                    }
                    sb.append("📎 [来源").append(count).append("] ").append(title).append("\n");
                    sb.append("   链接: ").append(url).append("\n");
                    sb.append("   内容: ").append(content).append("\n\n");
                }
            }

            sb.append("══════════════════════════════════════\n");
            sb.append("【再次强调】请基于以上搜索结果回答，标注信息来源编号。\n");
            sb.append("【重要】回答末尾必须列出所有参考来源的完整URL链接，格式如下：\n");
            sb.append("  参考来源：\n");
            sb.append("  [来源1] 标题 — https://...\n");
            sb.append("  [来源2] 标题 — https://...\n");
            sb.append("  确保用户可以直接点击链接打开网页。\n");
            sb.append("══════════════════════════════════════\n");

            String result = sb.toString();
            log.info("联网搜索结果长度: {} 字符", result.length());
            return result;

        } catch (Exception e) {
            log.error("联网搜索失败: {}", e.toString());
            return buildEmptyResult();
        }
    }

    private String buildSearchQuery(String userQuery) {
        boolean hasYear = userQuery.matches(".*(20\\d{2}|今年|最新|近年).*");

        if (hasYear) {
            return userQuery + " 高考 志愿填报";
        }

        if (userQuery.contains("分数线") || userQuery.contains("录取") || userQuery.contains("分数")) {
            return userQuery + " 2025 2024 最新";
        }
        if (userQuery.contains("专业") || userQuery.contains("就业")) {
            return userQuery + " 2025 最新";
        }
        if (userQuery.contains("政策") || userQuery.contains("规则")) {
            return userQuery + " 2025 最新政策";
        }

        return userQuery + " 高考 2025";
    }

    private String buildEmptyResult() {
        return "\n\n【联网搜索未获取到有效结果，请如实告知用户当前无法检索到相关实时信息，建议用户访问官方渠道查询，不要编造数据。】\n";
    }
}
