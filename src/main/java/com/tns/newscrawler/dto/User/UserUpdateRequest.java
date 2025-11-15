package com.tns.newscrawler.dto.User;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String fullName;
    private String email;
    private String password;
    private String role;
    private Boolean isActive;
}
