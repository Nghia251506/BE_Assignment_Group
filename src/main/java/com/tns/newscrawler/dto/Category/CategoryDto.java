package com.tns.newscrawler.dto.Category;

import lombok.Data;

@Data
public class CategoryDto {
    private Long id;
    private Long tenantId;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
}
