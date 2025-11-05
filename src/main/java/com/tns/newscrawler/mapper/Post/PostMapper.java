package com.tns.newscrawler.mapper.Post;

import com.tns.newscrawler.dto.Post.PostDto;
import com.tns.newscrawler.entity.Post;

public class PostMapper {
    public static PostDto toDto(Post p) {
        if (p == null) return null;
        PostDto d = new PostDto();
        d.setId(p.getId());
        d.setTenantId(p.getTenant()!=null ? p.getTenant().getId():null);
        d.setSourceId(p.getSource()!=null ? p.getSource().getId():null);
        d.setCategoryId(p.getCategory()!=null ? p.getCategory().getId():null);
        d.setOriginUrl(p.getOriginUrl());
        d.setTitle(p.getTitle());
        d.setSlug(p.getSlug());
        d.setSummary(p.getSummary());
        d.setContent(p.getContent());
        d.setThumbnail(p.getThumbnail());
        d.setStatus(p.getStatus().name());
        d.setDeleteStatus(p.getDeleteStatus().name());
        d.setPublishedAt(p.getPublishedAt());
        d.setViewCount(p.getViewCount());
        d.setCreatedAt(p.getCreatedAt());
        d.setUpdatedAt(p.getUpdatedAt());
        return d;
    }
}
