package com.smile.blue_blog.repository;

import com.smile.blue_blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // ========== 基础查询方法 ==========
    List<Article> findByAuthorIdOrderByCreateTimeDesc(Long authorId);
    List<Article> findByStatusOrderByCreateTimeDesc(Integer status);
    List<Article> findByCategoryAndStatusOrderByCreateTimeDesc(String category, Integer status);

    // ========== 新增查询方法 - 分页版本 ==========
    Page<Article> findByAuthorIdOrderByCreateTimeDesc(Long authorId, Pageable pageable);
    List<Article> findByAuthorIdAndStatusOrderByCreateTimeDesc(Long authorId, Integer status);
    Page<Article> findByAuthorIdAndStatusOrderByCreateTimeDesc(Long authorId, Integer status, Pageable pageable);

    // ========== 安全增强：公开接口专用查询方法 ==========

    /**
     * 根据ID和状态查找文章（公开接口使用）
     */
    Optional<Article> findByIdAndStatus(Long id, Integer status);

    /**
     * 获取已发布文章的标签（去重）
     */
    @Query("SELECT DISTINCT a.tags FROM Article a WHERE a.status = 1 AND a.tags IS NOT NULL AND a.tags != ''")
    List<String> findDistinctTagsByStatusPublished();

    /**
     * 获取指定分类下已发布文章的标签（去重）
     */
    @Query("SELECT DISTINCT a.tags FROM Article a WHERE a.category = :category AND a.status = 1 AND a.tags IS NOT NULL AND a.tags != ''")
    List<String> findDistinctTagsByCategoryAndStatus(@Param("category") String category);

    /**
     * 获取已发布文章的所有分类（去重）
     */
    @Query("SELECT DISTINCT a.category FROM Article a WHERE a.status = 1 AND a.category IS NOT NULL")
    List<String> findDistinctCategoriesByStatusPublished();

    // ========== 权限验证查询 ==========
    @Query("SELECT a FROM Article a WHERE a.id = :id AND a.author.id = :authorId")
    Optional<Article> findByIdAndAuthorId(@Param("id") Long id, @Param("authorId") Long authorId);

    // ========== 推荐和置顶查询 ==========
    List<Article> findByIsTopTrueAndStatusOrderByCreateTimeDesc(Integer status);
    List<Article> findByIsRecommendedTrueAndStatusOrderByCreateTimeDesc(Integer status);

    // ========== 分页查询 ==========
    Page<Article> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);
    Page<Article> findByCategoryAndStatusOrderByCreateTimeDesc(String category, Integer status, Pageable pageable);

    // ========== 搜索功能（使用参数化查询避免SQL注入） ==========
    @Query("SELECT a FROM Article a WHERE a.status = :status AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Article> searchArticles(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    // ========== 热门文章 ==========
    Page<Article> findByStatusOrderByViewCountDesc(Integer status, Pageable pageable);

    // ========== 统计功能 ==========
    long countByStatus(Integer status);
    Long countByAuthorIdAndStatus(Long authorId, Integer status);
    Long countByCategoryAndStatus(String category, Integer status);

    /**
     * 统计指定标签和状态的文档数量（优化版）
     */
    @Query("SELECT COUNT(a) FROM Article a WHERE a.status = :status AND a.tags LIKE CONCAT('%', :tag, '%')")
    Long countByTagAndStatus(@Param("tag") String tag, @Param("status") Integer status);

    /**
     * 统计指定分类、标签和状态的文档数量（优化版）
     */
    @Query("SELECT COUNT(a) FROM Article a WHERE a.category = :category AND a.status = :status AND a.tags LIKE CONCAT('%', :tag, '%')")
    Long countByCategoryAndTagAndStatus(@Param("category") String category, @Param("tag") String tag, @Param("status") Integer status);

    // ========== 标签相关查询 ==========
    @Query("SELECT DISTINCT a.category FROM Article a WHERE a.status = 1")
    List<String> findAllActiveCategories();

    // ========== 检查文章是否存在且属于指定作者 ==========
    boolean existsByIdAndAuthorId(Long id, Long authorId);

    // ========== 作者相关查询（私有接口使用） ==========

    /**
     * 获取作者的所有标签（包含所有状态的文章）
     */
    @Query("SELECT DISTINCT a.tags FROM Article a WHERE a.author.id = :authorId AND a.tags IS NOT NULL AND a.tags != ''")
    List<String> findDistinctTagsByAuthor(@Param("authorId") Long authorId);

    /**
     * 获取作者的所有分类（包含所有状态的文章）
     */
    @Query("SELECT DISTINCT a.category FROM Article a WHERE a.author.id = :authorId AND a.category IS NOT NULL")
    List<String> findDistinctCategoriesByAuthor(@Param("authorId") Long authorId);

    /**
     * 检查用户是否有指定文章的权限
     */
    @Query("SELECT COUNT(a) > 0 FROM Article a WHERE a.id = :articleId AND a.author.id = :authorId")
    boolean hasArticlePermission(@Param("articleId") Long articleId, @Param("authorId") Long authorId);
}