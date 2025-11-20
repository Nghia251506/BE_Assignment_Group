package com.tns.newscrawler.service.Tag;

import com.tns.newscrawler.dto.Tag.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TagService {
    List<TagDto> listTags();  // Lấy tất cả tags thay vì theo tenant

    TagDto getById(Long id);

    TagDto create(TagCreateRequest req);

    TagDto update(Long id, TagUpdateRequest req);

    void delete(Long id);

    // Tiện cho crawler/admin: upsert theo name trong tenant
    TagDto upsertByName(String name);

    List<TagDto> suggestTags(String keyword, int limit);

    TagDto getBySlug(String slug);

    // Admin
    Page<TagDto> searchAdmin(String keyword, Pageable pageable);

    TagDto createTag(TagDto dto);

    TagDto updateTag(Long id, TagDto dto);

    void deleteTag(Long id);
}
