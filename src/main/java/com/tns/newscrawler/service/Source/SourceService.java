package com.tns.newscrawler.service.Source;

import com.tns.newscrawler.dto.Source.SourceCreateRequest;
import com.tns.newscrawler.dto.Source.SourceDto;
import com.tns.newscrawler.dto.Source.SourceUpdateRequest;

import java.util.List;

public interface SourceService {

    List<SourceDto> getByTenant(Long tenantId);

    List<SourceDto> getActiveByTenant(Long tenantId);

    SourceDto getById(Long id);

    SourceDto create(SourceCreateRequest req);

    SourceDto update(Long id, SourceUpdateRequest req);

    void delete(Long id);

    // cho crawler lấy tất cả nguồn active
    List<SourceDto> getAllActive();
}
