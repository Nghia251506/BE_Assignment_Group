package com.tns.newscrawler.dto.Post;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class PostDto {
    private Long id;
    private Long tenantId;
    private Long sourceId;
    private Long categoryId;
    private String originUrl;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String thumbnail;
    private String status;        // pending/draft/published/removed
    private String deleteStatus;  // ACTIVE/DELETED
    private LocalDateTime publishedAt;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String baseUrl;
    private String categoryName;
    private String sourceName;
}
