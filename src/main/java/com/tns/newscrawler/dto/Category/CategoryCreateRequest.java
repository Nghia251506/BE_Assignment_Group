package com.tns.newscrawler.dto.Category;

import lombok.Data;

@Data
public class CategoryCreateRequest {
    private Long tenantId;     // bắt buộc
    private String code;
    private String name;
    private String description;
}
