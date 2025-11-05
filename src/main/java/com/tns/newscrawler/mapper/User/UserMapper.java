package com.tns.newscrawler.mapper.User;

import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.entity.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setTenantId(user.getTenant() != null ? user.getTenant().getId() : null);
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setIsActive(user.getIsActive());
        return dto;
    }
}
