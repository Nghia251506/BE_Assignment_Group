package com.tns.newscrawler.mapper.Auth;


import com.tns.newscrawler.dto.Auth.RoleDto;
import com.tns.newscrawler.entity.Permission;
import com.tns.newscrawler.entity.Role;

import java.util.stream.Collectors;

public class RoleMapper {

    public static RoleDto toDto(Role role) {
        if (role == null) return null;

        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());

        if (role.getPermissions() != null) {
            dto.setPermissions(
                    role.getPermissions()
                            .stream()
                            .map(Permission::getCode)
                            .collect(Collectors.toSet())
            );
        }

        return dto;
    }
}
