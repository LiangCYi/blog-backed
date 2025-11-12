package com.smile.blue_blog.controller;

import com.smile.blue_blog.dto.CreateArticleRequest;
import com.smile.blue_blog.dto.UpdateArticleRequest;
import com.smile.blue_blog.entity.Article;
import com.smile.blue_blog.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    // 标签相关接口
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        List<String> tags = articleService.findAllTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/tags/category/{category}")
    public ResponseEntity<List<String>> getTagsByCategory(@PathVariable String category) {
        List<String> tags = articleService.findTagsByCategory(category);
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/tags/author/{authorId}")
    public ResponseEntity<List<String>> getTagsByAuthor(@PathVariable Long authorId) {
        List<String> tags = articleService.findTagsByAuthor(authorId);
        return ResponseEntity.ok(tags);
    }

    // 分类相关接口
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = articleService.findAllCategories();
        return ResponseEntity.ok(categories);
    }

    // 文章查询接口
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Article>> getArticlesByTag(@PathVariable String tag) {
        List<Article> articles = articleService.findByTagAndStatus(tag, 1);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/category/{category}/tag/{tag}")
    public ResponseEntity<List<Article>> getArticlesByCategoryAndTag(
            @PathVariable String category,
            @PathVariable String tag) {
        List<Article> articles = articleService.findByCategoryAndTagAndStatus(category, tag, 1);
        return ResponseEntity.ok(articles);
    }

    // 统计接口
    @GetMapping("/count/tag/{tag}")
    public ResponseEntity<Long> countByTag(@PathVariable String tag) {
        long count = articleService.countByTagAndStatus(tag, 1);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/category/{category}/tag/{tag}")
    public ResponseEntity<Long> countByCategoryAndTag(
            @PathVariable String category,
            @PathVariable String tag) {
        long count = articleService.countByCategoryAndTagAndStatus(category, tag, 1);
        return ResponseEntity.ok(count);
    }

    /**
     * 发表文章
     */
    @PostMapping
    public ResponseEntity<?> createArticle(@Valid @RequestBody CreateArticleRequest request,
                                           HttpServletRequest httpRequest) {
        try {
            Long authorId = (Long) httpRequest.getAttribute("userId");
            Article savedArticle = articleService.createArticle(request, authorId);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "文章发表成功",
                    "data", savedArticle
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "发表文章失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable Long id,
                                           @Valid @RequestBody UpdateArticleRequest request,
                                           HttpServletRequest httpRequest) {
        try {
            Long authorId = (Long) httpRequest.getAttribute("userId");
            Article updatedArticle = articleService.updateArticle(id, request, authorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "文章更新成功",
                    "data", updatedArticle
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id,
                                           HttpServletRequest httpRequest) {
        try {
            Long authorId = (Long) httpRequest.getAttribute("userId");
            articleService.deleteArticle(id, authorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "文章删除成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getArticleDetail(@PathVariable Long id) {
        try {
            Article article = articleService.getArticleDetail(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", article
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取我的文章列表
     */
    @GetMapping("/my-articles")
    public ResponseEntity<?> getMyArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            HttpServletRequest httpRequest) {

        try {
            Long authorId = (Long) httpRequest.getAttribute("userId");
            Page<Article> articles = articleService.getUserArticles(authorId, status, page, size);

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
     * 搜索文章
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<Article> articles = articleService.searchArticles(keyword, status, page, size);

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
                    "message", "搜索失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取推荐文章
     */
    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedArticles() {
        try {
            List<Article> articles = articleService.getRecommendedArticles();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", articles
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "获取推荐文章失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取置顶文章
     */
    @GetMapping("/top")
    public ResponseEntity<?> getTopArticles() {
        try {
            List<Article> articles = articleService.getTopArticles();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", articles
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "获取置顶文章失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取热门文章
     */
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<Article> articles = articleService.getPopularArticles(page, size);

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
                    "message", "获取热门文章失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 点赞文章
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeArticle(@PathVariable Long id) {
        try {
            articleService.likeArticle(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "点赞成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}