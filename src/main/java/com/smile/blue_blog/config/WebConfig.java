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

        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")  // 只拦截API路径
                .excludePathPatterns(
                        // 用户认证相关
                        "/api/users/login",
                        "/api/users/register",
                        "/api/users/check-username/**",
                        "/api/users/check-email/**",

                        // 文章公开查询接口
                        "/api/articles/tags",
                        "/api/articles/tags/**",
                        "/api/articles/categories",
                        "/api/articles/tag/**",
                        "/api/articles/category/**",
                        "/api/articles/count/**",
                        "/api/articles/search",
                        "/api/articles/recommended",
                        "/api/articles/top",
                        "/api/articles/popular",
                        "/api/articles/{id}",  // 文章详情

                        // 其他公开接口
                        "/api/public/**",
                        "/api/comments/article/**",  // 文章评论查询
                        "/error",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );

        System.out.println("JWT拦截器注册完成");
        System.out.println("拦截路径: /api/**");
        System.out.println("排除路径: 登录、注册、文章查询等公开接口");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");


        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }


}