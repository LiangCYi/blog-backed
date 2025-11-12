package com.smile.blue_blog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String content;

    private LocalDateTime createTime;

    // 与 User 的关联关系
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;

    // 与 Article 的关联关系（如果需要）
    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    // 构造方法
    public Comment() {}

    public Comment(String content, User author) {
        this.content = content;
        this.author = author;
        this.createTime = LocalDateTime.now();
    }

    // getter 和 setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
