package com.lee.agentgazjku.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 历史对话响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponse {
    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 消息列表
     */
    private List<MessageDTO> messages;

    /**
     * 消息总数
     */
    private Integer total;
}

