package com.tns.newscrawler.dto.Category;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CategoryDto {
    private Long id;
    private Long parentId;
    private String slug;
    private String seoTitle;
    private String seoDescription;
    private String seoKeywords;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
}
