package com.smile.blue_blog.service;

import com.smile.blue_blog.dto.CreateArticleRequest;
import com.smile.blue_blog.dto.UpdateArticleRequest;
import com.smile.blue_blog.entity.Article;
import com.smile.blue_blog.entity.User;
import com.smile.blue_blog.repository.ArticleRepository;
import com.smile.blue_blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    // ========== 现有的查询方法 ==========

    /**
     * 获取所有唯一的标签
     */
    public List<String> findAllTags() {
        List<Article> articles = articleRepository.findByStatusOrderByCreateTimeDesc(1);

        return articles.stream()
                .map(Article::getTags)
                .filter(tags -> tags != null && !tags.trim().isEmpty())
                .flatMap(tags -> Arrays.stream(tags.split("\\s*,\\s*")))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取所有分类
     */
    public List<String> findAllCategories() {
        // 使用 Repository 的专用方法
        return articleRepository.findAllActiveCategories();
    }

    /**
     * 根据标签查询文章
     */
    public List<Article> findByTagAndStatus(String tag, Integer status) {
        List<Article> articles = articleRepository.findByStatusOrderByCreateTimeDesc(status);

        return articles.stream()
                .filter(article -> article.getTags() != null &&
                        article.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    /**
     * 根据分类和标签查询文章
     */
    public List<Article> findByCategoryAndTagAndStatus(String category, String tag, Integer status) {
        List<Article> articles = articleRepository.findByCategoryAndStatusOrderByCreateTimeDesc(category, status);

        return articles.stream()
                .filter(article -> article.getTags() != null &&
                        article.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    /**
     * 统计相关方法
     */
    public long countByStatus(Integer status) {
        return articleRepository.countByStatus(status);
    }

    public long countByCategoryAndStatus(String category, Integer status) {
        return articleRepository.countByCategoryAndStatus(category, status);
    }

    public long countByTagAndStatus(String tag, Integer status) {
        List<Article> articles = articleRepository.findByStatusOrderByCreateTimeDesc(status);
        return articles.stream()
                .filter(article -> article.getTags() != null &&
                        article.getTags().contains(tag))
                .count();
    }

    public long countByCategoryAndTagAndStatus(String category, String tag, Integer status) {
        List<Article> articles = articleRepository.findByCategoryAndStatusOrderByCreateTimeDesc(category, status);
        return articles.stream()
                .filter(article -> article.getTags() != null &&
                        article.getTags().contains(tag))
                .count();
    }

    /**
     * 获取某个分类下的所有标签
     */
    public List<String> findTagsByCategory(String category) {
        List<Article> articles = articleRepository.findByCategoryAndStatusOrderByCreateTimeDesc(category, 1);

        return articles.stream()
                .map(Article::getTags)
                .filter(tags -> tags != null && !tags.trim().isEmpty())
                .flatMap(tags -> Arrays.stream(tags.split("\\s*,\\s*")))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取作者的所有标签
     */
    public List<String> findTagsByAuthor(Long authorId) {
        List<Article> articles = articleRepository.findByAuthorIdOrderByCreateTimeDesc(authorId);

        return articles.stream()
                .map(Article::getTags)
                .filter(tags -> tags != null && !tags.trim().isEmpty())
                .flatMap(tags -> Arrays.stream(tags.split("\\s*,\\s*")))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ========== 新增文章管理方法 ==========

    /**
     * 创建文章
     */
    public Article createArticle(CreateArticleRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setCategory(request.getCategory());
        article.setStatus(request.getStatus());
        article.setAuthor(author);

        // 处理标签列表
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            String tagsString = String.join(",", request.getTags());
            article.setTags(tagsString);
        }

        // 自动生成摘要
        if (request.getSummary() == null || request.getSummary().trim().isEmpty()) {
            String content = article.getContent().replaceAll("#", "").replaceAll("```.*?```", "");
            String summary = content.length() > 200 ? content.substring(0, 200) + "..." : content;
            article.setSummary(summary);
        } else {
            article.setSummary(request.getSummary());
        }

        return articleRepository.save(article);
    }

    /**
     * 更新文章
     */
    public Article updateArticle(Long articleId, UpdateArticleRequest request, Long authorId) {
        // 使用 Repository 的权限验证方法
        Article article = articleRepository.findByIdAndAuthorId(articleId, authorId)
                .orElseThrow(() -> new RuntimeException("文章不存在或无权修改"));

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setCategory(request.getCategory());
        article.setStatus(request.getStatus());

        // 处理标签列表
        if (request.getTags() != null) {
            if (request.getTags().isEmpty()) {
                article.setTags(null);
            } else {
                String tagsString = String.join(",", request.getTags());
                article.setTags(tagsString);
            }
        }

        // 处理摘要
        if (request.getSummary() != null) {
            article.setSummary(request.getSummary());
        }

        return articleRepository.save(article);
    }

    /**
     * 删除文章
     */
    public void deleteArticle(Long articleId, Long authorId) {
        // 使用 Repository 的权限验证方法
        Article article = articleRepository.findByIdAndAuthorId(articleId, authorId)
                .orElseThrow(() -> new RuntimeException("文章不存在或无权删除"));

        articleRepository.delete(article);
    }

    /**
     * 获取文章详情
     */
    public Article getArticleDetail(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        article.incrementViewCount();
        articleRepository.save(article);

        return article;
    }

    /**
     * 获取用户文章列表（分页）
     */
    public Page<Article> getUserArticles(Long authorId, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());

        if (status != null) {
            return articleRepository.findByAuthorIdAndStatusOrderByCreateTimeDesc(authorId, status, pageable);
        }
        return articleRepository.findByAuthorIdOrderByCreateTimeDesc(authorId, pageable);
    }

    /**
     * 搜索文章
     */
    public Page<Article> searchArticles(String keyword, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return articleRepository.searchArticles(keyword, status != null ? status : 1, pageable);
    }

    /**
     * 获取推荐文章
     */
    public List<Article> getRecommendedArticles() {
        return articleRepository.findByIsRecommendedTrueAndStatusOrderByCreateTimeDesc(1);
    }

    /**
     * 获取置顶文章
     */
    public List<Article> getTopArticles() {
        return articleRepository.findByIsTopTrueAndStatusOrderByCreateTimeDesc(1);
    }

    /**
     * 获取热门文章（分页）
     */
    public Page<Article> getPopularArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findByStatusOrderByViewCountDesc(1, pageable);
    }

    /**
     * 点赞文章
     */
    public void likeArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        article.incrementLikeCount();
        articleRepository.save(article);
    }

    /**
     * 验证文章权限
     */
    public boolean checkArticlePermission(Long articleId, Long authorId) {
        return articleRepository.existsByIdAndAuthorId(articleId, authorId);
    }
}