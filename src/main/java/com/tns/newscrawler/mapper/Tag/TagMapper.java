package com.tns.newscrawler.mapper.Tag;

import com.tns.newscrawler.dto.Tag.TagDto;
import com.tns.newscrawler.entity.Tag;

public class TagMapper {
    public static TagDto toDto(Tag entity) {
        if (entity == null) return null;
        TagDto dto = new TagDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSlug(entity.getSlug());
        dto.setSeoTitle(entity.getSeoTitle());
        dto.setSeoDescription(entity.getSeoDescription());
        dto.setSeoKeywords(entity.getSeoKeywords());
        return dto;
    }

    public static void updateEntity(TagDto dto, Tag entity) {
        entity.setName(dto.getName());
        entity.setSlug(dto.getSlug());
        entity.setSeoTitle(dto.getSeoTitle());
        entity.setSeoDescription(dto.getSeoDescription());
        entity.setSeoKeywords(dto.getSeoKeywords());
    }

    public static Tag toEntity(TagDto dto) {
        if (dto == null) return null;
        Tag entity = new Tag();
        updateEntity(dto, entity);
        return entity;
    }
}
