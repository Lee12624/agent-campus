package com.lee.agentgazjku.chatmemory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlBasedChatMemory implements ChatMemory {

    private final JdbcTemplate jdbcTemplate;

    public MysqlBasedChatMemory(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initializeSchema();
    }

    private void initializeSchema() {
        String sql = """
            CREATE TABLE IF NOT EXISTS conversation_messages (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                conversation_id VARCHAR(255) NOT NULL,
                message JSON NOT NULL,
                message_timestamp BIGINT NOT NULL,  -- 存储前端传递的时间戳
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;
        jdbcTemplate.execute(sql);
    }

    @Override
    public List<Message> get(String conversationId) {
        String sql = "SELECT message, created_at FROM conversation_messages WHERE conversation_id = ? ORDER BY id ASC";
        return jdbcTemplate.query(sql, new MessageRowMapper(), conversationId);
    }

    @Override
    public void add(String conversationId, Message message) {
        addWithTimestamp(conversationId, message, System.currentTimeMillis());
    }

    /**
     * 添加消息并指定时间戳（用于前端传递的时间戳）
     */
    public void addWithTimestamp(String conversationId, Message message, long timestamp) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // 构造一个简化的 JSON 结构用于存储
            Map<String, Object> simplifiedMessage = new HashMap<>();
            simplifiedMessage.put("text", message.getText());
            simplifiedMessage.put("messageType", message.getClass().getSimpleName().replace("Message", "").toUpperCase());

            String jsonMessage = mapper.writeValueAsString(simplifiedMessage);

            // 改进的去重检查：基于对话ID、消息内容、消息类型和时间戳范围（允许5秒内的重复）
            String checkSql = "SELECT COUNT(*) FROM conversation_messages WHERE conversation_id = ? AND JSON_EXTRACT(message, '$.text') = ? AND JSON_EXTRACT(message, '$.messageType') = ? AND ABS(message_timestamp - ?) <= 5000";
            String messageText = message.getText();
            String messageType = message.getClass().getSimpleName().replace("Message", "").toUpperCase();
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, conversationId, messageText, messageType, timestamp);

            if (count != null && count > 0) {
                // 消息已存在，跳过插入
                System.out.println("跳过重复消息: " + conversationId + " - " + messageType + " - 时间戳: " + timestamp + " - " + messageText.substring(0, Math.min(50, messageText.length())));
                return;
            }

            // 插入新消息，包含时间戳
            String insertSql = "INSERT INTO conversation_messages (conversation_id, message, message_timestamp) VALUES (?, ?, ?)";
            int rowsAffected = jdbcTemplate.update(insertSql, conversationId, jsonMessage, timestamp);
            System.out.println("插入新消息: " + conversationId + " - " + messageType + " - 时间戳: " + timestamp + " - 影响行数: " + rowsAffected);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing message to JSON", e);
        }
    }


    @Override
    public void add(String conversationId, List<Message> messages) {
        messages.forEach(msg -> add(conversationId, msg));
    }

    @Override
    public void clear(String conversationId) {
        String deleteSql = "DELETE FROM conversation_messages WHERE conversation_id = ?";
        jdbcTemplate.update(deleteSql, conversationId);
    }

    /**
     * 清除所有对话的历史记录
     */
    public void clearAll() {
        String deleteSql = "DELETE FROM conversation_messages";
        jdbcTemplate.update(deleteSql);
    }

    /**
     * 获取所有对话的列表
     * @return 对话列表，包含每个对话的最后一条用户消息预览
     */
    public List<Map<String, Object>> getConversationList() {
        String sql = """
            SELECT
                conversation_id,
                COUNT(*) as message_count,
                MAX(message_timestamp) as last_message_timestamp,
                SUBSTRING_INDEX(GROUP_CONCAT(
                    CASE
                        WHEN JSON_EXTRACT(message, '$.messageType') = 'USER'
                        THEN JSON_EXTRACT(message, '$.text')
                        ELSE NULL
                    END
                    ORDER BY message_timestamp DESC SEPARATOR '|||'
                ), '|||', 1) as last_user_message
            FROM conversation_messages
            GROUP BY conversation_id
            ORDER BY last_message_timestamp DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("conversationId", rs.getString("conversation_id"));
            result.put("messageCount", rs.getInt("message_count"));
            // 使用message_timestamp字段，如果为0则使用created_at作为后备
            long messageTimestamp = rs.getLong("last_message_timestamp");
            if (messageTimestamp == 0) {
                messageTimestamp = rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").getTime() : System.currentTimeMillis();
            }
            result.put("lastMessageTimestamp", messageTimestamp);
            String lastUserMessage = rs.getString("last_user_message");
            if (lastUserMessage != null && lastUserMessage.length() > 50) {
                lastUserMessage = lastUserMessage.substring(0, 50) + "...";
            }
            result.put("lastUserMessagePreview", lastUserMessage);
            return result;
        });
    }

    private static class MessageRowMapper implements RowMapper<Message> {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            String json = rs.getString("message");
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode node = objectMapper.readTree(json);

                // 从 JSON 中提取文本内容和消息类型
                String content = node.path("text").asText();
                String messageType = node.path("messageType").asText();

                switch (messageType.toUpperCase()) {
                    case "USER":
                        return new UserMessage(content);
                    case "ASSISTANT":
                        return new AssistantMessage(content);
                    case "SYSTEM":
                        return new SystemMessage(content);
                    default:
                        return new UserMessage(content); // fallback to user message
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to parse message from DB", e);
            }
        }
    }

    /**
     * 获取带有时间戳的消息列表
     */
    public List<Map<String, Object>> getMessagesWithTimestamp(String conversationId) {
        String sql = "SELECT message, message_timestamp, created_at FROM conversation_messages WHERE conversation_id = ? ORDER BY message_timestamp ASC, id ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            String json = rs.getString("message");
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                JsonNode node = objectMapper.readTree(json);
                result.put("content", node.path("text").asText());
                result.put("messageType", node.path("messageType").asText());
                // 优先使用message_timestamp，如果没有则使用created_at
                long timestamp = rs.getLong("message_timestamp");
                if (timestamp == 0) {
                    timestamp = rs.getTimestamp("created_at").getTime();
                }
                result.put("timestamp", timestamp);
                return result;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse message from DB", e);
            }
        }, conversationId);
    }
}