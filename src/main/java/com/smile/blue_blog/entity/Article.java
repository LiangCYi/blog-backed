package com.smile.blue_blog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String coverImage;

    @Column(nullable = false, length = 50)
    private String category;

    // 简单字符串存储标签
    @Column(length = 500)
    private String tags;

    private Integer viewCount = 0;

    private Integer likeCount = 0;

    private Integer commentCount = 0;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private User author;

    // 新增字段
    @Column(length = 1000)
    private String summary;

    @Column(name = "is_top")
    private Boolean isTop = false;

    @Column(name = "is_recommended")
    private Boolean isRecommended = false;

    @Column(length = 100)
    private String password;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updateTime = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // 辅助方法处理标签
    public List<String> getTagList() {
        if (tags == null || tags.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(tags.split("\\s*,\\s*"));
    }

    public void setTagList(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagList);
        }
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }

    public void incrementCommentCount() {
        this.commentCount = (this.commentCount == null ? 0 : this.commentCount) + 1;
    }
}