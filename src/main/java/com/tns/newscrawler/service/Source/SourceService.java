package com.tns.newscrawler.service.Source;

import com.tns.newscrawler.dto.Source.SourceCreateRequest;
import com.tns.newscrawler.dto.Source.SourceDto;
import com.tns.newscrawler.dto.Source.SourceUpdateRequest;

import java.util.List;

public interface SourceService {

    List<SourceDto> getActiveByTenant();  // Đã bỏ tenant, lấy tất cả active sources

    SourceDto getById(Long id);

    SourceDto create(SourceCreateRequest req);

    SourceDto update(Long id, SourceUpdateRequest req);

    void delete(Long id);

    // Lấy tất cả nguồn active
    List<SourceDto> getAllActive();
}
