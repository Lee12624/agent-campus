package com.lee.agentgazjku.tools;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 限制工具只能访问沙箱目录下的文件名，防止路径穿越。
 */
public final class SafeFileName {

    private SafeFileName() {
    }

    /**
     * @return 拼好且已校验的路径；非法则返回 null
     */
    public static String resolveUnderDir(String dir, String fileName) {
        if (dir == null || fileName == null || fileName.isBlank()) {
            return null;
        }
        String base = new File(fileName.trim()).getName();
        if (base.isEmpty() || ".".equals(base) || "..".equals(base)) {
            return null;
        }
        Path root = Paths.get(dir).toAbsolutePath().normalize();
        Path full = root.resolve(base).normalize();
        if (!full.startsWith(root)) {
            return null;
        }
        return full.toString();
    }
}
