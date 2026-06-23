package com.lee.agentgazjku.config;


import com.lee.agentgazjku.chatmemory.MysqlBasedChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * ChatMemory配置类
 * 配置基于数据库的ChatMemory Bean
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * 基于MySQL数据库的ChatMemory Bean。
     * 启动时可选将 {@code 0|*} 遗留会话合并到指定用户前缀，避免登录后侧栏历史为空。
     */
    @Bean("mysqlChatMemory")
    public ChatMemory mysqlChatMemory(
            JdbcTemplate jdbcTemplate,
            @Value("${app.chat.merge-zero-prefix-into-user-id:0}") long mergeZeroPrefixIntoUserId) {
        MysqlBasedChatMemory memory = new MysqlBasedChatMemory(jdbcTemplate);
        if (mergeZeroPrefixIntoUserId > 0) {
            memory.mergeZeroPrefixIntoUser(mergeZeroPrefixIntoUserId);
        }
        return memory;
    }

}

