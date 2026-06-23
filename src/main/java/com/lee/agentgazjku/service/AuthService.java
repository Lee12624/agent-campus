package com.lee.agentgazjku.service;

import com.lee.agentgazjku.auth.JwtService;
import com.lee.agentgazjku.auth.PasswordHasher;
import com.lee.agentgazjku.repository.UserRepository;
import org.springframework.stereotype.Service;

import cn.hutool.core.util.RandomUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public void register(String username, String password) {
        if (username == null || username.length() < 3 || username.length() > 64) {
            throw new IllegalArgumentException("用户名长度需在 3～64 之间");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        String salt = RandomUtil.randomString(32);
        String md5 = PasswordHasher.hash(salt, password);
        userRepository.insert(username, salt, md5);
    }

    public String login(String username, String password) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (!PasswordHasher.matches(user.salt(), password, user.passwordMd5())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return jwtService.createToken(user.id(), user.username());
    }
}
