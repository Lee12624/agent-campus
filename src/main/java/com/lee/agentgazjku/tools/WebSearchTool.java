package com.lee.agentgazjku.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class WebSearchTool {

    // SearchAPI 的搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "联网搜索工具：用于查询实时信息、新闻、政策、网页内容。输入一个查询词，返回最多5条搜索结果。")
    public String searchWeb(
            @ToolParam(description = "搜索关键词，例如：2026年考研国家线 最新消息") String query) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");
        try {
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");

            if (organicResults == null || organicResults.isEmpty()) {
                log.info("联网搜索无结果: {}", query);
                return "未检索到相关联网结果，请尝试更具体的关键词。";
            }

            List<Object> objects = organicResults.stream().limit(5).collect(Collectors.toList());
            // 拼接搜索结果为字符串
            String result = objects.stream().map(obj -> {
                JSONObject tmpJSONObject = (JSONObject) obj;
                return tmpJSONObject.toString();
            }).collect(Collectors.joining(","));
            return result;
        } catch (Exception e) {
            log.warn("联网搜索失败: {}, error={}", query, e.getMessage());
            return "联网搜索失败：" + e.getMessage();
        }
    }
}
