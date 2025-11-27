package com.tns.newscrawler.mapper.Category;

import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.entity.Category;

public class CategoryMapper {

    public static CategoryDto toDto(Category entity) {
        if (entity == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.setId(entity.getId());
        dto.setParentId(entity.getParentId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setSlug(entity.getSlug());
        dto.setSeoTitle(entity.getSeoTitle());
        dto.setSeoDescription(entity.getSeoDescription());
        dto.setSeoKeywords(entity.getSeoKeywords());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }

    public static void updateEntity(CategoryDto dto, Category entity) {
        entity.setParentId(dto.getParentId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setSlug(dto.getSlug());
        entity.setSeoTitle(dto.getSeoTitle());
        entity.setSeoDescription(dto.getSeoDescription());
        entity.setSeoKeywords(dto.getSeoKeywords());
        entity.setIsActive(dto.getIsActive());
    }

    public static Category toEntity(CategoryDto dto) {
        if (dto == null) return null;
        Category entity = new Category();
        updateEntity(dto, entity);
        return entity;
    }
}
