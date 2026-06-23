package com.lee.agentgazjku.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AmapWeatherTool {

    @Value("${amap.weather-api-key}")
    private String apiKey;

    @Value("${amap.weather-url}")
    private String weatherUrl;

    @Tool(description = "获取天气信息：查询指定城市的实时天气和预报")
    public String getWeather(
            @ToolParam(description = "城市名称，例如：北京、上海、张家口") String city) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("key", apiKey);
        paramMap.put("city", city);
        paramMap.put("extensions", "all");

        try {
            log.info("开始查询天气，城市：{}，API地址：{}", city, weatherUrl);
            String response = HttpUtil.get(weatherUrl, paramMap);
            log.info("天气API返回结果：{}", response);
            
            JSONObject jsonObject = JSONUtil.parseObj(response);
            
            String status = jsonObject.getStr("status");
            log.info("API状态码：{}", status);
            
            if (!"1".equals(status)) {
                String errorMsg = jsonObject.getStr("info", "未知错误");
                log.warn("天气查询失败：{}", errorMsg);
                return "查询失败：" + errorMsg;
            }

            JSONArray forecasts = jsonObject.getJSONArray("forecasts");
            if (forecasts == null || forecasts.isEmpty()) {
                log.warn("未查询到{}的天气信息", city);
                return "未查询到" + city + "的天气信息";
            }

            JSONObject forecast = forecasts.getJSONObject(0);
            String cityName = forecast.getStr("city", city);
            String reportTime = forecast.getStr("reporttime", "");
            
            JSONArray casts = forecast.getJSONArray("casts");
            if (casts == null || casts.isEmpty()) {
                log.warn("未查询到{}的天气详情", city);
                return "未查询到" + city + "的天气详情";
            }

            JSONObject todayCast = casts.getJSONObject(0);
            String weather = todayCast.getStr("dayweather", "未知");
            String temperature = todayCast.getStr("daytemp", "未知");
            String nightTemp = todayCast.getStr("nighttemp", "未知");
            String windDirection = todayCast.getStr("daywind", "未知");
            String windPower = todayCast.getStr("daypower", "未知");
            String week = todayCast.getStr("week", "未知");

            String weekDay = "";
            switch (week) {
                case "1": weekDay = "周一"; break;
                case "2": weekDay = "周二"; break;
                case "3": weekDay = "周三"; break;
                case "4": weekDay = "周四"; break;
                case "5": weekDay = "周五"; break;
                case "6": weekDay = "周六"; break;
                case "7": weekDay = "周日"; break;
                default: weekDay = week; break;
            }

            StringBuilder result = new StringBuilder();
            result.append("🌤️ ").append(cityName).append("天气预报\n");
            result.append("📅 ").append(weekDay).append("\n");
            result.append("⏰ 更新时间：").append(reportTime).append("\n");
            result.append("☀️ 白天天气：").append(weather).append("\n");
            result.append("🌡️ 白天气温：").append(temperature).append("°C\n");
            result.append("🌙 夜间气温：").append(nightTemp).append("°C\n");
            result.append("💨 风向：").append(windDirection).append("\n");
            result.append("🌪️ 风力：").append(windPower).append("级\n");
            
            if (casts.size() > 1) {
                result.append("\n📆 未来几天预报：\n");
                for (int i = 1; i < Math.min(casts.size(), 4); i++) {
                    JSONObject future = casts.getJSONObject(i);
                    String futureWeek = future.getStr("week", "未知");
                    String futureWeather = future.getStr("dayweather", "未知");
                    String futureTemp = future.getStr("daytemp", "未知");
                    
                    String futureWeekDay = "";
                    switch (futureWeek) {
                        case "1": futureWeekDay = "周一"; break;
                        case "2": futureWeekDay = "周二"; break;
                        case "3": futureWeekDay = "周三"; break;
                        case "4": futureWeekDay = "周四"; break;
                        case "5": futureWeekDay = "周五"; break;
                        case "6": futureWeekDay = "周六"; break;
                        case "7": futureWeekDay = "周日"; break;
                        default: futureWeekDay = futureWeek; break;
                    }
                    
                    result.append(futureWeekDay).append(": ").append(futureWeather).append(" ").append(futureTemp).append("°C\n");
                }
            }

            log.info("天气查询成功：{}", result.toString());
            return result.toString();

        } catch (Exception e) {
            log.error("天气查询异常: {}, error={}", city, e.getMessage(), e);
            return "天气查询失败：" + e.getMessage();
        }
    }
}
