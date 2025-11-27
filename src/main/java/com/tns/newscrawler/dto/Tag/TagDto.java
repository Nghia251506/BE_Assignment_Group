package com.tns.newscrawler.dto.Tag;
import lombok.Data;

@Data
public class TagDto {
    private Long id;
    private String name;
    private String slug;
    private String seoTitle;
    private String seoDescription;
    private String seoKeywords;
}
