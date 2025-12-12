package com.lee.agentgazjku.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebScrapingToolTest {

    @Test
    void scrapeWeb() {

        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://www.baidu.com";
        String result = webScrapingTool.scrapeWeb(url);
        Assertions.assertNotNull(result);
    }
}