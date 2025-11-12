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

    // 基础查询方法
    List<Article> findByAuthorIdOrderByCreateTimeDesc(Long authorId);
    List<Article> findByStatusOrderByCreateTimeDesc(Integer status);
    List<Article> findByCategoryAndStatusOrderByCreateTimeDesc(String category, Integer status);

    // 新增查询方法 - 分页版本
    Page<Article> findByAuthorIdOrderByCreateTimeDesc(Long authorId, Pageable pageable);
    List<Article> findByAuthorIdAndStatusOrderByCreateTimeDesc(Long authorId, Integer status);
    Page<Article> findByAuthorIdAndStatusOrderByCreateTimeDesc(Long authorId, Integer status, Pageable pageable);

    // 权限验证查询
    @Query("SELECT a FROM Article a WHERE a.id = :id AND a.author.id = :authorId")
    Optional<Article> findByIdAndAuthorId(@Param("id") Long id, @Param("authorId") Long authorId);

    // 推荐和置顶查询
    List<Article> findByIsTopTrueAndStatusOrderByCreateTimeDesc(Integer status);
    List<Article> findByIsRecommendedTrueAndStatusOrderByCreateTimeDesc(Integer status);

    // 分页查询
    Page<Article> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);
    Page<Article> findByCategoryAndStatusOrderByCreateTimeDesc(String category, Integer status, Pageable pageable);

    // 搜索功能（使用参数化查询避免SQL注入）
    @Query("SELECT a FROM Article a WHERE a.status = :status AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Article> searchArticles(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    // 热门文章
    Page<Article> findByStatusOrderByViewCountDesc(Integer status, Pageable pageable);

    // 统计功能
    long countByStatus(Integer status);
    Long countByAuthorIdAndStatus(Long authorId, Integer status);
    Long countByCategoryAndStatus(String category, Integer status);

    // 标签相关查询
    @Query("SELECT DISTINCT a.category FROM Article a WHERE a.status = 1")
    List<String> findAllActiveCategories();

    // 检查文章是否存在且属于指定作者
    boolean existsByIdAndAuthorId(Long id, Long authorId);
}