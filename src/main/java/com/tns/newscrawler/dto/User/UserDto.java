package com.tns.newscrawler.dto.User;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String roleCode;
    private String roleName;
    private Boolean isActive;
}
