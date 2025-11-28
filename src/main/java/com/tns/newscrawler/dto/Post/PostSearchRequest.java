package com.tns.newscrawler.dto.Post;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PostSearchRequest {
    private Long categoryId;     // optional
    private Long sourceId;       // optional
    private String keyword;      // search theo title
    private String status;       // mặc định published
    private Integer page = 0;    // 0-based
    private Integer size = 10;
    private String sort = "publishedAt,DESC"; // hoặc createdAt,DESC
}
