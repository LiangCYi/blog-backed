package com.smile.blue_blog.controller;

import com.smile.blue_blog.dto.CreateArticleRequest;
import com.smile.blue_blog.dto.UpdateArticleRequest;
import com.smile.blue_blog.entity.Article;
import com.smile.blue_blog.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/author/articles")
@RequiredArgsConstructor
public class AuthorArticleController {

    private final ArticleService articleService;

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
     * 获取我的文章列表（包含所有状态的文章）
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

    /**
     * 获取作者的所有标签（包含所有状态的文章）
     */
    @GetMapping("/tags/author/{authorId}")
    public ResponseEntity<List<String>> getTagsByAuthor(@PathVariable Long authorId) {
        List<String> tags = articleService.findTagsByAuthor(authorId);
        return ResponseEntity.ok(tags);
    }

    /**
     * 获取文章详情（作者可以查看自己的所有文章，包括草稿）
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAuthorArticleDetail(@PathVariable Long id,
                                                    HttpServletRequest httpRequest) {
        try {
            Long authorId = (Long) httpRequest.getAttribute("userId");
            Article article = articleService.getAuthorArticleDetail(id, authorId);
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
}