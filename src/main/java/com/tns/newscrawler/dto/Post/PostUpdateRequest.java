package com.tns.newscrawler.dto.Post;

import lombok.Data;

import java.util.List;

@Data
public class PostUpdateRequest {
    private Long categoryId;

    private String title;
    private String slug;

    private String summary;
    private String content;
    private String contentRaw;      // NEW: nếu admin muốn chỉnh luôn bản raw

    private String thumbnail;

    // optional: chuyển draft/published/removed
    private String status;

    // SEO fields – NEW
    private String seoTitle;
    private String seoDescription;
    private String seoKeywords;
    private List<Long> tagIds;

    // NEW: canonical
    private String canonicalUrl;
}
