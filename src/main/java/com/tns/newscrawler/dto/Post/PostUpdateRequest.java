package com.tns.newscrawler.dto.Post;
import lombok.Data;

@Data
public class PostUpdateRequest {
    private Long categoryId;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String thumbnail;
    private String status; // optional: chuyển draft/published/removed
}
