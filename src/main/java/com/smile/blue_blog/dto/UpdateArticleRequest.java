package com.smile.blue_blog.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateArticleRequest {
    private String title;
    private String content;
    private String coverImage;
    private String category;
    private List<String> tags; // 前端传递标签列表
    private Integer status;

    // 新增字段
    private String summary;
    private String password;
    private Boolean isTop;
    private Boolean isRecommended;
}