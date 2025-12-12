package com.lee.agentgazjku.controller.dto;

import lombok.Data;

/**
 * 消息DTO
 * 用于API请求和响应
 */
@Data
public class MessageDTO {
    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：USER, ASSISTANT, SYSTEM
     */
    private String messageType;

    /**
     * 消息时间戳
     */
    private Long timestamp;
}

