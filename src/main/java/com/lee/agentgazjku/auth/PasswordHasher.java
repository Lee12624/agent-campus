package com.lee.agentgazjku.auth;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * 密码存储：MD5(盐 + 明文)。盐为随机串，与用户记录一并保存。
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String salt, String plainPassword) {
        return DigestUtil.md5Hex(salt + plainPassword);
    }

    public static boolean matches(String salt, String plainPassword, String storedMd5Hex) {
        if (storedMd5Hex == null || salt == null || plainPassword == null) {
            return false;
        }
        return storedMd5Hex.equalsIgnoreCase(hash(salt, plainPassword));
    }
}
