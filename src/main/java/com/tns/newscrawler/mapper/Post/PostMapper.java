package com.tns.newscrawler.mapper.Post;

import com.tns.newscrawler.dto.Post.PostDetailDto;
import com.tns.newscrawler.dto.Post.PostDto;
import com.tns.newscrawler.entity.Post;

public class PostMapper {

    public static PostDto toDto(Post p) {
        if (p == null) return null;

        PostDto d = new PostDto();
        d.setId(p.getId());

        // liên kết
        d.setSourceId(p.getSource() != null ? p.getSource().getId() : null);
        d.setCategoryId(p.getCategory() != null ? p.getCategory().getId() : null);

        // URL
        d.setOriginUrl(p.getOriginUrl());
        d.setCanonicalUrl(p.getCanonicalUrl());

        // nội dung
        d.setTitle(p.getTitle());
        d.setSlug(p.getSlug());
        d.setSummary(p.getSummary());
        d.setContent(p.getContent());
        d.setContentRaw(p.getContentRaw());
        d.setThumbnail(p.getThumbnail());

        // trạng thái
        d.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        d.setDeleteStatus(p.getDeleteStatus() != null ? p.getDeleteStatus().name() : null);

        d.setPublishedAt(p.getPublishedAt());

        // SEO
        d.setSeoTitle(p.getSeoTitle());
        d.setSeoDescription(p.getSeoDescription());
        d.setSeoKeywords(p.getSeoKeywords());

        // thống kê & audit
        d.setViewCount(p.getViewCount());
        d.setCreatedAt(p.getCreatedAt());
        d.setUpdatedAt(p.getUpdatedAt());

        // info tiện cho view
        if (p.getSource() != null) {
            d.setBaseUrl(p.getSource().getBaseUrl());
            d.setSourceName(p.getSource().getName());
        }

        if (p.getCategory() != null) {
            d.setCategoryName(p.getCategory().getName());
        }

        return d;
    }

    public static PostDetailDto toDetailDto(Post entity) {
        if (entity == null) return null;

        PostDetailDto dto = new PostDetailDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setSlug(entity.getSlug());
        dto.setSummary(entity.getSummary());
        dto.setContent(entity.getContent());
        dto.setThumbnail(entity.getThumbnail());
        dto.setPublishedAt(entity.getPublishedAt());
        dto.setViewCount(entity.getViewCount());

        dto.setSeoTitle(entity.getSeoTitle());
        dto.setSeoDescription(entity.getSeoDescription());
        dto.setSeoKeywords(entity.getSeoKeywords());
        dto.setCanonicalUrl(entity.getCanonicalUrl());

        // Category + tags sẽ được set thêm ở service (join từ Category/Tag)
        return dto;
    }
}
