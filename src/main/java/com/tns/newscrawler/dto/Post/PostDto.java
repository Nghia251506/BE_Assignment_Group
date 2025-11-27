package com.tns.newscrawler.dto.Post;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDto {
    private Long id;
    private Long sourceId;
    private Long categoryId;

    private String originUrl;
    private String canonicalUrl;   // NEW: map với Post.canonicalUrl

    private String title;
    private String slug;

    private String summary;
    private String content;
    private String contentRaw;     // NEW: map với Post.contentRaw

    private String thumbnail;

    // pending/draft/published/removed
    private String status;

    // ACTIVE/DELETED
    private String deleteStatus;

    private LocalDateTime publishedAt;

    // SEO fields
    private String seoTitle;
    private String seoDescription;
    private String seoKeywords;

    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Các field tiện cho view/list
    private String baseUrl;
    private String categoryName;
    private String sourceName;
}
