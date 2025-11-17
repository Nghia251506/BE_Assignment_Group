package com.tns.newscrawler.mapper.Category;

import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.entity.Category;

public class CategoryMapper {

    public static CategoryDto toDto(Category c) {
        if (c == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setTenantId(c.getTenant() != null ? c.getTenant().getId() : null);
        dto.setCode(c.getCode());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        dto.setIsActive(c.getIsActive());
        dto.setSlug(c.getSlug());
        return dto;
    }
}
