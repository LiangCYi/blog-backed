package com.smile.blue_blog.utils;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;  //2小时

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationInBlueBlogProject2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认24小时
    private long expiration;

    // 生成token
    public String generateToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // 从token中获取用户名
    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    // 从token中获取用户ID
    public Long getUserIdFromToken(String token) {
        return getAllClaimsFromToken(token).get("userId", Long.class);
    }

    // 获取token中的所有claims
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    // 验证token是否有效
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token已过期" + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token验证失败" + e.getMessage());
            return false;
        }
    }

    // 检查token是否过期
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getAllClaimsFromToken(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            System.out.println("检测token过期状态：" + e.getMessage());
            return true;
        }
    }

    //获取token剩余有效时间
    public long getRemainingTime(String token) {
        try {
            Date expiration = getAllClaimsFromToken(token).getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }




}