package com.lee.agentgazjku.controller;

import com.lee.agentgazjku.auth.JwtAuthFilter;
import com.lee.agentgazjku.controller.dto.AuthResponse;
import com.lee.agentgazjku.controller.dto.LoginRequest;
import com.lee.agentgazjku.repository.UserRepository;
import com.lee.agentgazjku.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @Resource
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody LoginRequest body) {
        try {
            authService.register(body.getUsername(), body.getPassword());
            return ResponseEntity.ok(new AuthResponse(true, "注册成功", null, null, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage(), null, null, null));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "用户名已存在", null, null, null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest body) {
        try {
            String token = authService.login(body.getUsername(), body.getPassword());
            var user = userRepository.findByUsername(body.getUsername()).orElseThrow();
            return ResponseEntity.ok(new AuthResponse(true, "登录成功", token, user.id(), user.username()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(new AuthResponse(false, e.getMessage(), null, null, null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(HttpServletRequest request) {
        Object uid = request.getAttribute(JwtAuthFilter.ATTR_USER_ID);
        if (!(uid instanceof Number n)) {
            return ResponseEntity.status(401).build();
        }
        long userId = n.longValue();
        return userRepository.findById(userId)
                .map(u -> ResponseEntity.ok(new AuthResponse(true, "ok", null, u.id(), u.username())))
                .orElseGet(() -> ResponseEntity.status(401).build());
    }
}
