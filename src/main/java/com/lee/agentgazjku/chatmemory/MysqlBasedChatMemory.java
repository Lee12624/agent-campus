package com.lee.agentgazjku.chatmemory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class MysqlBasedChatMemory implements ChatMemory {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        migrateLegacyConversationIds();
    }

    /**
     * 旧数据无用户前缀，统一归为 userId=0（无法登录对应，仅避免与登录用户串会话）
     */
    private void migrateLegacyConversationIds() {
        try {
            int n = jdbcTemplate.update(
                    "UPDATE conversation_messages SET conversation_id = CONCAT('0|', conversation_id) WHERE conversation_id NOT LIKE '%|%'"
            );
            if (n > 0) {
                log.info("已迁移 {} 条历史消息为 userId=0 前缀", n);
            }
        } catch (Exception e) {
            log.warn("迁移历史 conversation_id 跳过: {}", e.getMessage());
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        String sql = "SELECT message FROM conversation_messages WHERE conversation_id = ? ORDER BY message_timestamp ASC, id ASC";
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
            // 构造一个简化的 JSON 结构用于存储
            Map<String, Object> simplifiedMessage = new HashMap<>();
            simplifiedMessage.put("text", message.getText());
            simplifiedMessage.put("messageType", message.getClass().getSimpleName().replace("Message", "").toUpperCase());

            String jsonMessage = objectMapper.writeValueAsString(simplifiedMessage);

            // 改进的去重检查：基于对话ID、消息内容、消息类型和时间戳范围（允许5秒内重复判定）
            // 注意：JSON_EXTRACT 返回 JSON 值，比较前需 JSON_UNQUOTE。
            String checkSql = """
                    SELECT COUNT(*) FROM conversation_messages
                    WHERE conversation_id = ?
                    AND JSON_UNQUOTE(JSON_EXTRACT(message, '$.text')) = ?
                    AND JSON_UNQUOTE(JSON_EXTRACT(message, '$.messageType')) = ?
                    AND ABS(message_timestamp - ?) <= 5000
                    """;
            String messageText = message.getText();
            String messageType = message.getClass().getSimpleName().replace("Message", "").toUpperCase();
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, conversationId, messageText, messageType, timestamp);

            if (count != null && count > 0) {
                // 消息已存在，跳过插入
                String preview = messageText == null ? "" : messageText.substring(0, Math.min(50, messageText.length()));
                log.info("跳过重复消息: {} - {} - 时间戳: {} - {}", conversationId, messageType, timestamp, preview);
                return;
            }

            // 插入新消息，包含时间戳
            String insertSql = "INSERT INTO conversation_messages (conversation_id, message, message_timestamp) VALUES (?, ?, ?)";
            int rowsAffected = jdbcTemplate.update(insertSql, conversationId, jsonMessage, timestamp);
            log.debug("插入新消息: {} - {} - 时间戳: {} - 影响行数: {}", conversationId, messageType, timestamp, rowsAffected);
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
     * 将登录前迁移产生的 {@code 0|UUID} 会话划归指定用户，使其与 {@code 用户ID|UUID} 规则一致。
     * 可幂等执行；多人共用后端时请改为仅对您的主账号 id 执行一次（见配置 {@code app.chat.merge-zero-prefix-into-user-id}）。
     */
    public int mergeZeroPrefixIntoUser(long userId) {
        if (userId <= 0) {
            return 0;
        }
        String prefix = userId + "|";
        int n = jdbcTemplate.update(
                "UPDATE conversation_messages SET conversation_id = CONCAT(?, SUBSTRING(conversation_id, 3)) WHERE conversation_id LIKE '0|%'",
                prefix);
        if (n > 0) {
            log.info("已将 {} 条消息从 0| 前缀合并为 {} 前缀", n, prefix);
        }
        return n;
    }

    /**
     * 清除当前用户在库中的全部会话
     */
    public void clearAllForUser(long userId) {
        String pattern = userId + "|%";
        jdbcTemplate.update("DELETE FROM conversation_messages WHERE conversation_id LIKE ?", pattern);
    }

    /**
     * 获取指定用户的对话列表
     * @return 对话列表，包含首条/最后一条用户消息预览；conversationId 为带前缀的库内主键
     */
    public List<Map<String, Object>> getConversationList(long userId) {
        String pattern = userId + "|%";
        // 使用相关子查询代替 GROUP_CONCAT(ORDER BY …)，避免部分 MySQL/MariaDB 版本或 sql_mode 下列表 SQL 失败导致侧栏一直为空
        String sql = """
            SELECT
                cm.conversation_id,
                COUNT(*) AS message_count,
                MAX(cm.message_timestamp) AS last_message_timestamp,
                MAX(cm.created_at) AS last_created_at,
                (
                    SELECT JSON_UNQUOTE(JSON_EXTRACT(sub.message, '$.text'))
                    FROM conversation_messages sub
                    WHERE sub.conversation_id = cm.conversation_id
                      AND JSON_UNQUOTE(JSON_EXTRACT(sub.message, '$.messageType')) = 'USER'
                    ORDER BY sub.message_timestamp ASC, sub.id ASC
                    LIMIT 1
                ) AS first_user_message,
                (
                    SELECT JSON_UNQUOTE(JSON_EXTRACT(sub.message, '$.text'))
                    FROM conversation_messages sub
                    WHERE sub.conversation_id = cm.conversation_id
                      AND JSON_UNQUOTE(JSON_EXTRACT(sub.message, '$.messageType')) = 'USER'
                    ORDER BY sub.message_timestamp DESC, sub.id DESC
                    LIMIT 1
                ) AS last_user_message
            FROM conversation_messages cm
            WHERE cm.conversation_id LIKE ?
            GROUP BY cm.conversation_id
            ORDER BY MAX(cm.message_timestamp) DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("conversationId", rs.getString("conversation_id"));
            long mc = rs.getLong("message_count");
            result.put("messageCount", mc > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) mc);
            // 使用message_timestamp字段，如果为0则使用created_at作为后备
            long messageTimestamp = rs.getLong("last_message_timestamp");
            if (messageTimestamp == 0) {
                messageTimestamp = rs.getTimestamp("last_created_at") != null
                        ? rs.getTimestamp("last_created_at").getTime()
                        : System.currentTimeMillis();
            }
            result.put("lastMessageTimestamp", messageTimestamp);
            String firstUserMessage = rs.getString("first_user_message");
            if (firstUserMessage != null && firstUserMessage.length() > 50) {
                firstUserMessage = firstUserMessage.substring(0, 50) + "...";
            }
            result.put("firstUserMessagePreview", firstUserMessage);
            String lastUserMessage = rs.getString("last_user_message");
            if (lastUserMessage != null && lastUserMessage.length() > 50) {
                lastUserMessage = lastUserMessage.substring(0, 50) + "...";
            }
            result.put("lastUserMessagePreview", lastUserMessage);
            return result;
        }, pattern);
    }

    private static class MessageRowMapper implements RowMapper<Message> {
        private static final ObjectMapper MAPPER = new ObjectMapper();

        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            String json = rs.getString("message");
            try {
                JsonNode node = MAPPER.readTree(json);

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