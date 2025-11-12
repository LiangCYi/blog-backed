package com.smile.blue_blog.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateArticleRequest {
    private String title;
    private String content;
    private String coverImage;
    private String category;
    private List<String> tags; // 前端传递标签列表
    private Integer status = 1;

    private String Summary;
    private String password;
    private Boolean isTop = false;
    private Boolean isRecommended = false;
}