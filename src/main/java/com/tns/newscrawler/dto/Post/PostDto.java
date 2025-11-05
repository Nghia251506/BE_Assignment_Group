package com.tns.newscrawler.dto.Post;
import lombok.Data;
import java.time.LocalDateTime;

@Data
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
}
