package com.tns.newscrawler.dto.Post;

import com.tns.newscrawler.dto.Tag.TagDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Getter
@Setter
public class PostDetailDto {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String thumbnail;
    private LocalDateTime publishedAt;
    private Integer viewCount;

    private String seoTitle;
    private String seoDescription;
    private String seoKeywords;
    private String canonicalUrl;

    private SimpleCategoryDto category;
    private List<TagDto> tags;
    @Data
    @Getter
    @Setter
    public static class SimpleCategoryDto {
        private Long id;
        private String name;
        private String slug;
    }
}
