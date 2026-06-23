package com.lee.agentgazjku.auth;

/**
 * 将数据库中的会话主键与前端 chatId 隔离：存储为 {@code userId|chatId}。
 */
public final class ConversationScope {

    private ConversationScope() {
    }

    public static String scope(long userId, String clientChatId) {
        return userId + "|" + clientChatId;
    }

    public static String clientChatId(String scopedId) {
        if (scopedId == null) {
            return "";
        }
        int i = scopedId.indexOf('|');
        return i < 0 ? scopedId : scopedId.substring(i + 1);
    }

    public static void validateClientChatId(String chatId) {
        if (chatId == null || chatId.isBlank() || chatId.contains("|") || chatId.length() > 128) {
            throw new IllegalArgumentException("无效的会话ID");
        }
    }
}
