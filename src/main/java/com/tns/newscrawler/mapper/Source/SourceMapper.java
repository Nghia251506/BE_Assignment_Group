package com.tns.newscrawler.mapper.Source;

import com.tns.newscrawler.dto.Source.SourceDto;
import com.tns.newscrawler.entity.Source;

public class SourceMapper {

    public static SourceDto toDto(Source s) {
        if (s == null) return null;
        SourceDto dto = new SourceDto();
        dto.setId(s.getId());
        dto.setCategoryId(s.getCategory() != null ? s.getCategory().getId() : null);
        dto.setName(s.getName());
        dto.setBaseUrl(s.getBaseUrl());
        dto.setListUrl(s.getListUrl());
        dto.setListItemSelector(s.getListItemSelector());
        dto.setLinkAttr(s.getLinkAttr());
        dto.setTitleSelector(s.getTitleSelector());
        dto.setContentSelector(s.getContentSelector());
        dto.setThumbnailSelector(s.getThumbnailSelector());
        dto.setAuthorSelector(s.getAuthorSelector());
        dto.setIsActive(s.getIsActive());
        dto.setNote(s.getNote());
        dto.setCategoryName(s.getCategory().getName());
        return dto;
    }
}
