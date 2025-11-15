package com.tns.newscrawler.mapper.Auth;

import com.tns.newscrawler.dto.Auth.PermissionDto;
import com.tns.newscrawler.entity.Permission;

public class PermissionMapper {

    public static PermissionDto toDto(Permission p) {
        if (p == null) return null;

        PermissionDto dto = new PermissionDto();
        dto.setId(p.getId());
        dto.setCode(p.getCode());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        return dto;
    }
}
