package com.smile.blue_blog.dto;

import com.smile.blue_blog.entity.Article;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ArticleDTO {
    private Long id;
    private String title;
    private String content;
    private String coverImage;
    private String category;
    private List<String> tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private UserDTO author;

    public static ArticleDTO fromEntity(Article article) {
        if (article == null) return null;

        ArticleDTO dto = new ArticleDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setCoverImage(article.getCoverImage());
        dto.setCategory(article.getCategory());
        dto.setTags(article.getTagList()); // 使用辅助方法
        dto.setViewCount(article.getViewCount());
        dto.setLikeCount(article.getLikeCount());
        dto.setCommentCount(article.getCommentCount());
        dto.setStatus(article.getStatus());
        dto.setCreateTime(article.getCreateTime());
        dto.setUpdateTime(article.getUpdateTime());

        if (article.getAuthor() != null) {
            dto.setAuthor(UserDTO.fromEntity(article.getAuthor()));
        }

        return dto;
    }
}