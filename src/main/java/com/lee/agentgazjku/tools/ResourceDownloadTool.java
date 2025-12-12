package com.lee.agentgazjku.tools;


import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;

import com.lee.agentgazjku.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

public class ResourceDownloadTool {

    @Tool(description = "Download a resource from a URL and save it to a specified file")
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url,
                                   @ToolParam(description = "Name of the file to save the downloaded resource") String fileName) {

        String fileDir = FileConstant.FILE_SAVE_PATH + "/download";
        String filePath = fileDir + "/" + fileName;

        try {
            FileUtil.mkdir(fileDir);
            HttpUtil.downloadFile(url, new File(filePath));
            return "File downloaded successfully to " + filePath;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }

    }
}
