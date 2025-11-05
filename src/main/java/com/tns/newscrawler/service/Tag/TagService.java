package com.tns.newscrawler.service.Tag;

import com.tns.newscrawler.dto.Tag.*;

import java.util.List;

public interface TagService {
    List<TagDto> listByTenant(Long tenantId);
    TagDto getById(Long id);
    TagDto create(TagCreateRequest req);
    TagDto update(Long id, TagUpdateRequest req);
    void delete(Long id);

    // tiện cho crawler/admin: upsert theo name trong tenant
    TagDto upsertByName(Long tenantId, String name);
}
