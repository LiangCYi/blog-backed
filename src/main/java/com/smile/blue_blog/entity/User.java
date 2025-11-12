package com.smile.blue_blog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "users")
@EqualsAndHashCode(of = "id")
public class User {

    public static final int USERNAME_MAX_LENGTH = 50;
    public static final int EMAIL_MAX_LENGTH = 100;
    public static final int NICKNAME_MAX_LENGTH = 50;
    public static final int BIO_MAX_LENGTH = 500;
    public static final int ROLE_MAX_LENGTH = 20;

    public static final String DEFAULT_ROLE = "USER";
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_DISABLED = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = USERNAME_MAX_LENGTH)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(unique = true, length = EMAIL_MAX_LENGTH)
    private String email;

    @Column(length = NICKNAME_MAX_LENGTH)
    private String nickname;

    private String avatar;

    @Column(length = BIO_MAX_LENGTH)
    private String bio;

    @Column(length = ROLE_MAX_LENGTH)
    private String role = DEFAULT_ROLE;

    private Integer status = STATUS_ACTIVE;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // 修正关联关系 - 移除 cascade 或简化配置
    @JsonIgnore
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Article> articles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updatedTime = now;

        if (role == null) {
            role = DEFAULT_ROLE;
        }
        if (status == null) {
            status = STATUS_ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    // 业务方法
    public boolean isActive() {
        return STATUS_ACTIVE == status;
    }

    public void disable() {
        this.status = STATUS_DISABLED;
    }

    public void enable() {
        this.status = STATUS_ACTIVE;
    }

    public void updateLastLoginTime() {
        this.lastLoginTime = LocalDateTime.now();
    }

    public String getDisplayName() {
        return nickname != null && !nickname.trim().isEmpty() ? nickname : username;
    }
}