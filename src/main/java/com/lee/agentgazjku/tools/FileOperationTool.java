package com.lee.agentgazjku.tools;

import cn.hutool.core.io.FileUtil;

import com.lee.agentgazjku.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class FileOperationTool {
    private final String filePath = FileConstant.FILE_SAVE_PATH + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName) {
        String newFileName = SafeFileName.resolveUnderDir(filePath, fileName);
        if (newFileName == null) {
            return "Error reading file: invalid file name";
        }
        try {
            return FileUtil.readUtf8String(newFileName);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }

    }

    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of a file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {

        String newFileName = SafeFileName.resolveUnderDir(filePath, fileName);
        if (newFileName == null) {
            return "Error writing file: invalid file name";
        }
        FileUtil.mkdir(filePath);
        try {
            FileUtil.writeUtf8String(content, newFileName);
            return "File written successfully to " + newFileName;
        } catch (Exception e) {
            return "Error writing file: " + e.getMessage();
        }
    }

}
