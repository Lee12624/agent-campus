package com.lee.agentgazjku.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 校验 JWT：支持 {@code Authorization: Bearer} 或 SSE 使用的 {@code ?access_token=}。
 */
@Component
@Order(2)
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String ATTR_USER_ID = "authUserId";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String ctx = request.getContextPath();
        if (path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }

        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        Optional<Long> uid = jwtService.parseUserId(token);
        if (uid.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"success\":false,\"message\":\"未登录或登录已过期\"}");
            return;
        }

        request.setAttribute(ATTR_USER_ID, uid.get());
        filterChain.doFilter(request, response);
    }

    private static boolean isPublicPath(String path) {
        return path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/doc.html")
                || path.startsWith("/webjars/")
                || path.startsWith("/ai/download/pdf")
                || path.equals("/favicon.ico")
                || path.startsWith("/download/pdf");
    }

    private static String extractToken(HttpServletRequest request) {
        String h = request.getHeader("Authorization");
        if (h != null && h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return h.substring(7).trim();
        }
        String q = request.getParameter("access_token");
        return q != null ? q.trim() : "";
    }
}
