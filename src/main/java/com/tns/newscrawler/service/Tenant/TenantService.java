package com.tns.newscrawler.service.Tenant;

import com.tns.newscrawler.dto.Tenant.TenantCreateRequest;
import com.tns.newscrawler.dto.Tenant.TenantDto;
import com.tns.newscrawler.dto.Tenant.TenantUpdateRequest;

import java.util.List;

public interface TenantService {
    List<TenantDto> getAll();
    TenantDto getById(Long id);
    TenantDto create(TenantCreateRequest req);
    TenantDto update(Long id, TenantUpdateRequest req);
    void delete(Long id);
}
