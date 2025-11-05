package com.tns.newscrawler.mapper.Tenant;

import com.tns.newscrawler.dto.Tenant.TenantDto;
import com.tns.newscrawler.entity.Tenant;

public class TenantMapper {

    public static TenantDto toDto(Tenant tenant) {
        if (tenant == null) return null;
        TenantDto dto = new TenantDto();
        dto.setId(tenant.getId());
        dto.setCode(tenant.getCode());
        dto.setName(tenant.getName());
        dto.setContactEmail(tenant.getContactEmail());
        dto.setContactPhone(tenant.getContactPhone());
        dto.setMaxUsers(tenant.getMaxUsers());
        dto.setStatus(tenant.getStatus().name());
        return dto;
    }
}
