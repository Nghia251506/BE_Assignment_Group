package com.tns.newscrawler.service.Tag;

import com.tns.newscrawler.dto.Tag.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TagService {
    List<TagDto> listByTenant(Long tenantId);
    TagDto getById(Long id);
    TagDto create(TagCreateRequest req);
    TagDto update(Long id, TagUpdateRequest req);
    void delete(Long id);

    // tiện cho crawler/admin: upsert theo name trong tenant
    TagDto upsertByName(Long tenantId, String name);
    List<TagDto> suggestTags(Long tenantId, String keyword, int limit);

    TagDto getBySlug(Long tenantId, String slug);

//    Page<PostSummaryDto> getPostsByTagSlug(Long tenantId, String slug, Pageable pageable);

    // Admin
    Page<TagDto> searchAdmin(Long tenantId, String keyword, Pageable pageable);

    TagDto createTag(Long tenantId, TagDto dto);

    TagDto updateTag(Long tenantId, Long id, TagDto dto);

    void deleteTag(Long tenantId, Long id)
}
