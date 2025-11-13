package com.smile.blue_blog.controller;

import com.smile.blue_blog.entity.Article;
import com.smile.blue_blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/articles")
@RequiredArgsConstructor
public class PublicArticleController {

    private final ArticleService articleService;

    // ========== 新增：文章列表查询接口 ==========

    /**
     * 获取已发布文章列表（分页）
     */
    @GetMapping
    public ResponseEntity<?> getPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag) {

        try {
            Page<Article> articles;

            if (category != null && tag != null) {
                // 根据分类和标签查询
                articles = articleService.findByCategoryAndTagAndStatus(category, tag, 1, page, size);
            } else if (category != null) {
                // 根据分类查询
                articles = articleService.findByCategoryAndStatus(category, 1, page, size);
            } else if (tag != null) {
                // 根据标签查询
                articles = articleService.findByTagAndStatus(tag, 1, page, size);
            } else {
                // 查询所有已发布文章
                articles = articleService.findByStatus(1, page, size);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", articles.getContent(),
                    "pagination", Map.of(
                            "page", articles.getNumber(),
                            "size", articles.getSize(),
                            "total", articles.getTotalElements(),
                            "totalPages", articles.getTotalPages()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "获取文章列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据分类获取已发布文章列表
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getArticlesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<Article> articles = articleService.findByCategoryAndStatus(category, 1, page, size);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", articles.getContent(),
                    "pagination", Map.of(
                            "page", articles.getNumber(),
                            "size", articles.getSize(),
                            "total", articles.getTotalElements(),
                            "totalPages", articles.getTotalPages()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "获取分类文章失败: " + e.getMessage()
            ));
        }
    }

    // ========== 您原有的其他接口保持不变 ==========

    /**
     * 获取所有标签（只包含已发布文章的标签）
     */
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        List<String> tags = articleService.findPublishedTags();
        return ResponseEntity.ok(tags);
    }

    // ... 其他原有接口保持不变
}