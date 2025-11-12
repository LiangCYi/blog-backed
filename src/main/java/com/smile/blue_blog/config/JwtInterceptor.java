package com.smile.blue_blog.config;

import com.smile.blue_blog.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Value("${jwt.enabled:true}")
    private boolean jwtEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("=== JWT拦截器开始处理 ===");
        System.out.println("请求方法: " + request.getMethod());
        System.out.println("请求URI: " + request.getRequestURI());
        System.out.println("完整URL: " + request.getRequestURL().toString());

        // 临时禁用JWT验证
        if (!jwtEnabled) {
            System.out.println("JWT拦截器已禁用 - 放行请求: " + request.getRequestURI());
            // 即使禁用也设置一个测试用户，方便调试
            request.setAttribute("username", "testUser");
            request.setAttribute("userId", 1L);
            return true;
        }

        // 必须放行OPTIONS请求（CORS预检请求）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("放行OPTIONS预检请求");
            return true;
        }

        String requestURI = request.getRequestURI();
        System.out.println("请求路径: " + requestURI);

        // 放行公开接口
        if (isPublicPath(requestURI)) {
            System.out.println("放行公开接口: " + requestURI);
            return true;
        }

        // JWT 验证逻辑
        String token = extractTokenFromRequest(request);

        if (token == null) {
            System.out.println("未找到有效的Authorization头");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供访问令牌");
            return false;
        }

        try {
            if (jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);
                Long userId = jwtUtils.getUserIdFromToken(token);

                // 重要：在验证成功后设置请求属性
                request.setAttribute("username", username);
                request.setAttribute("userId", userId);

                System.out.println("JWT验证通过，用户: " + username + ", ID: " + userId);
                System.out.println("已设置请求属性 - username: " + username + ", userId: " + userId);
                return true;
            } else {
                System.out.println("JWT验证失败 - Token无效");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "令牌无效或已过期");
                return false;
            }
        } catch (ExpiredJwtException e) {
            System.out.println("JWT令牌已过期：" + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "登录已过期，请重新登录");
            return false;
        } catch (Exception e) {
            System.out.println("JWT验证异常: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "令牌验证异常: " + e.getMessage());
        }
        return false;
    }

    /**
     * 从请求中提取Token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization头: " + (authHeader != null ?
                authHeader.substring(0, Math.min(authHeader.length(), 50)) + "..." : "null"));

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("提取后的token: " + token.substring(0, Math.min(token.length(), 20)) + "...");
            return token;
        }

        // 也可以尝试从参数中获取token
        String paramToken = request.getParameter("token");
        if (paramToken != null && !paramToken.trim().isEmpty()) {
            System.out.println("🔑 从参数获取token: " + paramToken.substring(0, Math.min(paramToken.length(), 20)) + "...");
            return paramToken;
        }

        return null;
    }

    /**
     * 判断是否为公开路径
     */
    private boolean isPublicPath(String requestURI) {
        return requestURI.startsWith("/api/users/login") ||
                requestURI.startsWith("/api/users/register") ||
                requestURI.startsWith("/api/users/check-username") ||
                requestURI.startsWith("/api/users/check-email") ||
                requestURI.startsWith("/api/public/") ||
                requestURI.startsWith("/swagger-ui/") ||
                requestURI.startsWith("/v3/api-docs/") ||
                requestURI.startsWith("/webjars/") ||
                requestURI.equals("/error");
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = String.format(
                "{\"success\":false,\"message\":\"%s\",\"code\":%d,\"timestamp\":%d}",
                message, status, System.currentTimeMillis()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
        System.out.println("发送错误响应: " + jsonResponse);
    }
}