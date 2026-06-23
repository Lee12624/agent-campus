package com.lee.agentgazjku.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.lee.agentgazjku.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用本机或 classpath 中的 TTF/TTC 生成 PDF（iText 的 STSongStd-Light 等名称依赖完整字体程序，font-asian 包内仅有 CMap，易失败）。
 */
@Slf4j
public class PDFGenerationTool {
    // 保存UUID和原文件名的映射
    private static final Map<String, String> FILE_NAME_MAP = new ConcurrentHashMap<>();

    @Value("${app.server-address:http://localhost:8123}")
    private String serverAddress;

    @Tool(description = "根据给定正文生成 PDF 文件并保存到服务器 tmp/pdf 目录。fileName 仅取文件名，自动补 .pdf。")
    public String generatePDF(
            @ToolParam(description = "保存用的文件名，如 report.pdf") String fileName,
            @ToolParam(description = "写入 PDF 的正文内容") String content) {
        // 用UUID生成文件名，避免中文问题
        String uuid = UUID.randomUUID().toString();
        String uuidFileName = uuid + ".pdf";
        
        // 保存原文件名用于显示
        String displayFileName = sanitizeFileName(fileName);
        
        // 保存UUID和原文件名的映射
        FILE_NAME_MAP.put(uuid, displayFileName);
        
        String fileDir = FileConstant.FILE_SAVE_PATH + "/pdf";
        String filePath = fileDir + "/" + uuidFileName;
        try {
            FileUtil.mkdir(fileDir);
            PdfFont font = resolveChineseFont();
            try {
                log.debug("PDF 使用字体: {}", font.getFontProgram().getFontNames().getFontName());
            } catch (Exception ignored) {
                // 部分环境下字体元数据不可用，不影响生成
            }

            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                document.setFont(font);
                document.add(new Paragraph(content == null ? "" : content));
            }
            
            String downloadUrl = serverAddress + "/api/ai/download/pdf?fileName=" + uuidFileName;
            return "PDF 已生成！\n\n文件：" + displayFileName + "\n\n下载：" + downloadUrl + "\n\n直接复制上面的链接到浏览器地址栏就能下载。";
        } catch (IOException e) {
            log.warn("生成 PDF 失败: {}", e.getMessage());
            return "生成 PDF 失败: " + e.getMessage();
        }
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "export.pdf";
        }
        String base = new File(fileName.trim()).getName();
        if (base.isEmpty() || ".".equals(base)) {
            base = "export.pdf";
        }
        if (!base.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            base = base + ".pdf";
        }
        return base;
    }

    private static PdfFont resolveChineseFont() throws IOException {
        // 1) classpath：自行放入 resources/fonts/pdf-font.ttf 或 .otf
        try (InputStream in = PDFGenerationTool.class.getResourceAsStream("/fonts/pdf-font.ttf")) {
            if (in != null) {
                byte[] bytes = in.readAllBytes();
                return PdfFontFactory.createFont(
                        bytes,
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
        }
        try (InputStream in = PDFGenerationTool.class.getResourceAsStream("/fonts/pdf-font.otf")) {
            if (in != null) {
                byte[] bytes = in.readAllBytes();
                return PdfFontFactory.createFont(
                        bytes,
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
        }

        // 2) 环境变量：完整路径；.ttc 需带索引，如 C:/Windows/Fonts/simsun.ttc,0
        String env = System.getenv("ZJKU_PDF_FONT");
        if (env != null && !env.isBlank()) {
            String spec = env.trim();
            if (!spec.contains(",") && spec.toLowerCase(Locale.ROOT).endsWith(".ttc")) {
                spec = spec + ",0";
            }
            Path p = Paths.get(spec.contains(",") ? spec.substring(0, spec.lastIndexOf(',')) : spec);
            if (Files.isRegularFile(p)) {
                return PdfFontFactory.createFont(
                        spec,
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
        }

        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

        // 3) Windows 常见系统字体
        if (os.contains("win")) {
            Path simsun = Paths.get("C:/Windows/Fonts/simsun.ttc");
            if (Files.isRegularFile(simsun)) {
                return PdfFontFactory.createFont(
                        "C:/Windows/Fonts/simsun.ttc,0",
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
            Path msyh = Paths.get("C:/Windows/Fonts/msyh.ttc");
            if (Files.isRegularFile(msyh)) {
                return PdfFontFactory.createFont(
                        "C:/Windows/Fonts/msyh.ttc,0",
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
            Path simhei = Paths.get("C:/Windows/Fonts/simhei.ttf");
            if (Files.isRegularFile(simhei)) {
                return PdfFontFactory.createFont(
                        simhei.toString(),
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
        }

        // 4) Linux 常见开源字体
        if (os.contains("linux") || os.contains("nix")) {
            String[] candidates = {
                    "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
                    "/usr/share/fonts/truetype/arphic/uming.ttc",
                    "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
            };
            for (String path : candidates) {
                Path p = Paths.get(path);
                if (Files.isRegularFile(p)) {
                    String spec = path.endsWith(".ttc") ? path + ",0" : path;
                    return PdfFontFactory.createFont(
                            spec,
                            PdfEncodings.IDENTITY_H,
                            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                }
            }
        }

        throw new IOException(
                "未找到中文字体。可选方案：① 将任意中文 .ttf 放到 src/main/resources/fonts/pdf-font.ttf 后重新打包；"
                        + "② 设置环境变量 ZJKU_PDF_FONT 为字体路径（.ttc 用 路径,0）；"
                        + "③ 在 Windows 确认存在 C:\\\\Windows\\\\Fonts\\\\simsun.ttc。");
    }
    
    // 获取原文件名的方法（用于下载接口）
    public static String getDisplayName(String uuid) {
        return FILE_NAME_MAP.getOrDefault(uuid, "document.pdf");
    }
}
