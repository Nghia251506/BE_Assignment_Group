package com.tns.newscrawler.dto.Post;
import lombok.Data;

@Data
public class PostCreateRequest {
    private Long sourceId;
    private Long categoryId;
    private String originUrl;   // bắt buộc
    private String title;
    private String slug;        // optional, BE có thể tự gen
    private String summary;
    private String content;     // có thể để rỗng, crawler detail sẽ bổ sung
    private String contentRaw;  // crawler detail
    private String thumbnail;
}
