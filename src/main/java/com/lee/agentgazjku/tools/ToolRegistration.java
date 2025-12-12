package com.lee.agentgazjku.tools;


import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Bean
    public ToolCallback[] allTools() {
        // 注册所有工具
        TerminateTool terminateTool = new TerminateTool();
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        return ToolCallbacks.from(
            fileOperationTool,
            webScrapingTool,
            resourceDownloadTool,
            pdfGenerationTool,
            webSearchTool,
            terminateTool
        );
    }
}
