package com.lee.agentgazjku.auth;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtService {

    private final byte[] secret;
    private final long expireSeconds;

    public JwtService(
            @Value("${app.auth.jwt-secret}") String secretString,
            @Value("${app.auth.jwt-expire-seconds:604800}") long expireSeconds) {
        if (secretString == null || secretString.length() < 16) {
            throw new IllegalStateException("app.auth.jwt-secret 长度至少 16 字符");
        }
        this.secret = secretString.getBytes(StandardCharsets.UTF_8);
        this.expireSeconds = expireSeconds;
    }

    public String createToken(long userId, String username) {
        long now = System.currentTimeMillis() / 1000;
        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", userId);
        payload.put("uname", username);
        payload.put("iat", now);
        payload.put("exp", now + expireSeconds);
        return JWTUtil.createToken(payload, secret);
    }

    public Optional<Long> parseUserId(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        if (!JWTUtil.verify(token, secret)) {
            return Optional.empty();
        }
        JWT jwt = JWTUtil.parseToken(token);
        Object uid = jwt.getPayload("uid");
        if (uid == null) {
            return Optional.empty();
        }
        if (uid instanceof Number n) {
            return Optional.of(n.longValue());
        }
        try {
            return Optional.of(Long.parseLong(uid.toString()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
