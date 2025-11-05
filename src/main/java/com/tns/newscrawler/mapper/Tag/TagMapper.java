package com.tns.newscrawler.mapper.Tag;

import com.tns.newscrawler.dto.Tag.TagDto;
import com.tns.newscrawler.entity.Tag;

public class TagMapper {
    public static TagDto toDto(Tag t) {
        if (t == null) return null;
        TagDto d = new TagDto();
        d.setId(t.getId());
        d.setTenantId(t.getTenant()!=null ? t.getTenant().getId() : null);
        d.setName(t.getName());
        d.setSlug(t.getSlug());
        return d;
    }
}
