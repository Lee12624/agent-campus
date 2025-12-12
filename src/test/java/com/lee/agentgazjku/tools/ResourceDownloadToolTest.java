package com.lee.agentgazjku.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ResourceDownloadToolTest {

    @Test
    public void downloadResource() {
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();

        String url = "https://www.baidu.com";
        String fileName = "logo.png";
        String result = resourceDownloadTool.downloadResource(url, fileName);
        Assertions.assertNotNull( result);
    }
}