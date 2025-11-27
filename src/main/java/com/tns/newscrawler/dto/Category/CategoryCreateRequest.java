package com.tns.newscrawler.dto.Category;

import lombok.Data;

@Data
public class CategoryCreateRequest {
    private String code;
    private String name;
    private String description;
}
