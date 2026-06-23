package com.lee.agentgazjku.controller;


import com.lee.agentgazjku.app.CampusApp;
import com.lee.agentgazjku.auth.ConversationScope;
import com.lee.agentgazjku.auth.JwtAuthFilter;
import com.lee.agentgazjku.chatmemory.MysqlBasedChatMemory;
import com.lee.agentgazjku.controller.dto.ChatHistoryResponse;
import com.lee.agentgazjku.controller.dto.ConversationListItem;
import com.lee.agentgazjku.controller.dto.MessageDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    @Resource
    private CampusApp campusApp;

    @Resource(name = "mysqlChatMemory")
    private MysqlBasedChatMemory mysqlChatMemory;

    private static long currentUserId(HttpServletRequest request) {
        Object v = request.getAttribute(JwtAuthFilter.ATTR_USER_ID);
        if (v instanceof Number n) {
            return n.longValue();
        }
        throw new IllegalStateException("未认证");
    }


    /**
     * 执行流式对话，与校园学姐进行互动，返回校园学姐的回复内容流
     * @param userMessage 用户消息
     * @param chatId 对话ID，用于区分不同的对话会话
     * @return 校园学姐的回复内容流
     */

    @GetMapping(value = "/campus_app/chat/rag/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithVectorStoreStream(HttpServletRequest request,
            @RequestParam String userMessage,
            @RequestParam String chatId) {
        long userId = currentUserId(request);
        try {
            ConversationScope.validateClientChatId(chatId);
        } catch (IllegalArgumentException e) {
            return Flux.just("data: 参数错误：" + e.getMessage() + "\n\n", "data: [DONE]\n\n");
        }
        String scoped = ConversationScope.scope(userId, chatId);
        return campusApp.doChatWithVectorStoreStream(userMessage, scoped)
                .map(content -> {
                    // 清洗内容中可能包含的 data: 字样，防止与 SSE 协议标记混淆
                    String cleanedContent = content.replaceAll("(?m)^data:", "").replace("data: ", "");
                    return "data: " + cleanedContent + "\n\n";
                }) // 标准化SSE格式
                .concatWith(Flux.just("data: [DONE]\n\n")) // 添加结束标记
                .onErrorResume(e -> Flux.just(
                        "data: 服务异常：" + e.getMessage() + "\n\n",
                        "data: [DONE]\n\n"));
    }


    /**
     * 使用工具进行对话，返回一个包含工具调用结果的字符串流
     * 该方法会根据用户输入的问题，调用注册的工具，并返回一个包含工具调用结果的字符串流
     * @param userMessage 用户消息
     * @param chatId 对话ID，用于区分不同的对话会话
     * @return 工具调用结果的字符串流
     */
    @GetMapping(value = "/campus_app/chat/tool/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithToolStream(HttpServletRequest request,
            @RequestParam String userMessage,
            @RequestParam String chatId) {
        long userId = currentUserId(request);
        try {
            ConversationScope.validateClientChatId(chatId);
        } catch (IllegalArgumentException e) {
            return Flux.just("data: 参数错误：" + e.getMessage() + "\n\n", "data: [DONE]\n\n");
        }
        String scoped = ConversationScope.scope(userId, chatId);
        return campusApp.doChatWithToolStream(userMessage, scoped)
                .map(content -> {
                    // 清洗内容中可能包含的 data: 字样，防止与 SSE 协议标记混淆
                    String cleanedContent = content.replaceAll("(?m)^data:", "").replace("data: ", "");
                    return "data: " + cleanedContent + "\n\n";
                }) // 标准化SSE格式
                .concatWith(Flux.just("data: [DONE]\n\n")) // 添加结束标记
                .onErrorResume(e -> Flux.just(
                        "data: 服务异常：" + e.getMessage() + "\n\n",
                        "data: [DONE]\n\n"));
    }


    // ==================== 历史对话管理接口 ====================

    /**
     * 获取所有对话列表
     * @return 对话列表
     */
    @GetMapping("/chat/history/database/list")
    public ResponseEntity<List<ConversationListItem>> getConversationList(HttpServletRequest request) {
        try {
            long userId = currentUserId(request);
            List<Map<String, Object>> rawList = mysqlChatMemory.getConversationList(userId);
            List<ConversationListItem> conversationList = rawList.stream()
                    .map(item -> new ConversationListItem(
                            ConversationScope.clientChatId((String) item.get("conversationId")),
                            (String) item.get("firstUserMessagePreview"),
                            (String) item.get("lastUserMessagePreview"),
                            toLongObject(item.get("lastMessageTimestamp")),
                            toIntegerObject(item.get("messageCount"))
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(conversationList);
        } catch (Exception e) {
            log.warn("getConversationList failed for user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取基于数据库的历史对话
     * @param conversationId 对话ID
     * @return 历史对话列表
     */
    @GetMapping("/chat/history/database")
    public ResponseEntity<ChatHistoryResponse> getDatabaseHistory(HttpServletRequest request, @RequestParam String conversationId) {
        try {
            long userId = currentUserId(request);
            ConversationScope.validateClientChatId(conversationId);
            String scoped = ConversationScope.scope(userId, conversationId);
            List<Map<String, Object>> rawMessages = mysqlChatMemory.getMessagesWithTimestamp(scoped);
            List<MessageDTO> messageDTOs = rawMessages.stream()
                    .map(item -> {
                        MessageDTO dto = new MessageDTO();
                        dto.setContent((String) item.get("content"));
                        dto.setMessageType((String) item.get("messageType"));
                        dto.setTimestamp((Long) item.get("timestamp"));
                        return dto;
                    })
                    .collect(Collectors.toList());
            ChatHistoryResponse response = new ChatHistoryResponse(conversationId, messageDTOs, messageDTOs.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * 添加消息到基于数据库的历史对话
     * @param conversationId 对话ID
     * @param messageDTO 消息DTO
     * @return 操作结果
     */
    @PostMapping("/chat/history/database/add")
    public ResponseEntity<Map<String, Object>> addDatabaseMessage(
            HttpServletRequest request,
            @RequestParam String conversationId,
            @RequestBody MessageDTO messageDTO) {
        try {
            long userId = currentUserId(request);
            ConversationScope.validateClientChatId(conversationId);
            String scoped = ConversationScope.scope(userId, conversationId);
            Message message = convertToMessage(messageDTO);
            // 使用前端传递的时间戳，如果没有则使用当前时间
            long timestamp = messageDTO.getTimestamp() != null ? messageDTO.getTimestamp() : System.currentTimeMillis();
            mysqlChatMemory.addWithTimestamp(scoped, message, timestamp);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "消息添加成功");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "消息添加失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }


    /**
     * 清除基于数据库的历史对话
     * @param conversationId 对话ID
     * @return 操作结果
     */
        @DeleteMapping("/chat/history/database")
    public ResponseEntity<Map<String, Object>> clearDatabaseHistory(HttpServletRequest request, @RequestParam String conversationId) {
        try {
            long userId = currentUserId(request);
            ConversationScope.validateClientChatId(conversationId);
            mysqlChatMemory.clear(ConversationScope.scope(userId, conversationId));
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "历史对话清除成功");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "历史对话清除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 清除所有基于数据库的历史对话
     * @return 操作结果
     */
    @DeleteMapping("/chat/history/database/clearAll")
    public ResponseEntity<Map<String, Object>> clearAllDatabaseHistory(HttpServletRequest request) {
        try {
            mysqlChatMemory.clearAllForUser(currentUserId(request));
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "所有历史对话清除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "清除所有历史对话失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 将MessageDTO转换为Message对象
     */
    private Message convertToMessage(MessageDTO dto) {
        String content = dto.getContent() != null ? dto.getContent() : "";
        String messageType = dto.getMessageType() != null ? dto.getMessageType().toUpperCase() : "USER";

        return switch (messageType) {
            case "USER" -> new UserMessage(content);
            case "ASSISTANT" -> new AssistantMessage(content);
            case "SYSTEM" -> new SystemMessage(content);
            default -> new UserMessage(content);
        };
    }

    private static Long toLongObject(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer toIntegerObject(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            long l = n.longValue();
            return l > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) l;
        }
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
