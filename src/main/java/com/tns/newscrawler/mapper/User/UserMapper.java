package com.tns.newscrawler.mapper.User;

import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.entity.Role;
import com.tns.newscrawler.entity.User;

import java.util.stream.Collectors;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setIsActive(user.getIsActive());

        if (user.getRole() != null) {
            dto.setRoleCode(user.getRole().getCode());
            dto.setRoleName(user.getRole().getName());
        }

        return dto;
    }
}
