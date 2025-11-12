package com.smile.blue_blog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 改为所有路径，确保所有请求都能通过CORS
                .allowedOriginPatterns("*")  // 使用模式匹配，更灵活
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        System.out.println("CORS配置已启用 - 允许所有来源");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("=== 注册JWT拦截器 ===");

        // 使用更精确的拦截配置
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")  // 只拦截API路径
                .excludePathPatterns(
                        "/api/users/login",      // 登录接口
                        "/api/users/register",   // 注册接口
                        "/api/users/check-username/**",  // 用户名检查
                        "/api/users/check-email/**",     // 邮箱检查
                        "/api/public/**",        // 公开接口
                        "/error",                // 错误页面
                        "/swagger-ui/**",        // Swagger UI
                        "/v3/api-docs/**"        // API文档
                );

        System.out.println("JWT拦截器注册完成");
        System.out.println("拦截路径: /api/**");
        System.out.println("排除路径: 登录、注册、检查接口等公开接口");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");


        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }


}