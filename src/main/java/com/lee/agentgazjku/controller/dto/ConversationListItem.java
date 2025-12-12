package com.lee.agentgazjku.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话列表项DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationListItem {
    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 最后一条用户消息的预览（前50个字符）
     */
    private String lastUserMessagePreview;

    /**
     * 最后消息时间戳
     */
    private Long lastMessageTimestamp;

    /**
     * 消息总数
     */
    private Integer messageCount;
}



