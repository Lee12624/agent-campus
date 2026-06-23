package com.lee.agentgazjku.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(64) NOT NULL,
                    salt VARCHAR(64) NOT NULL,
                    password_md5 VARCHAR(32) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_username (username)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);
    }

    private static final RowMapper<UserRow> ROW_MAPPER = (rs, rowNum) -> new UserRow(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("salt"),
            rs.getString("password_md5")
    );

    public Optional<UserRow> findByUsername(String username) {
        var list = jdbcTemplate.query(
                "SELECT id, username, salt, password_md5 FROM users WHERE username = ?",
                ROW_MAPPER,
                username
        );
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<UserRow> findById(long id) {
        var list = jdbcTemplate.query(
                "SELECT id, username, salt, password_md5 FROM users WHERE id = ?",
                ROW_MAPPER,
                id
        );
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public long insert(String username, String salt, String passwordMd5) {
        jdbcTemplate.update(
                "INSERT INTO users (username, salt, password_md5) VALUES (?, ?, ?)",
                username,
                salt,
                passwordMd5
        );
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id != null ? id : 0L;
    }

    public record UserRow(long id, String username, String salt, String passwordMd5) {
    }
}
