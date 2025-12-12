package com.lee.agentgazjku.config;


import com.lee.agentgazjku.chatmemory.MysqlBasedChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
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
     * 基于MySQL数据库的ChatMemory Bean
     */
    @Bean("mysqlChatMemory")
    public ChatMemory mysqlChatMemory(JdbcTemplate jdbcTemplate) {
        return new MysqlBasedChatMemory(jdbcTemplate);
    }


}

