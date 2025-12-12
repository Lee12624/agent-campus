package com.lee.agentgazjku.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WebScrapingTool {

    @Tool(description = "Scrape web content")
    public String scrapeWeb(@ToolParam(description = "URL of the web page to scrape") String url){

        Document document = null;
        try {
            document = Jsoup.connect(url).get();
            return document.html();
        } catch (Exception e) {
            return "Error scraping web: " + e.getMessage();
        }


    }
}
