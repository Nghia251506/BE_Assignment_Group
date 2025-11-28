package com.tns.newscrawler.dto.Category;

import lombok.Data;

@Data
public class CategoryUpdateRequest {
    private Long parentId;
    private String name;
    private String description;
    private Boolean isActive;
}
