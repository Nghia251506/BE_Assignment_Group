package com.tns.newscrawler.entity.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDoc {
    private String id;
    private String title;
    private String summary;
    private String thumbnail;
    private String categoryName;
    private String sourceName;
    private String slug;
    private Long publishedAt; // Lưu dạng Timestamp (giây) để sắp xếp
}