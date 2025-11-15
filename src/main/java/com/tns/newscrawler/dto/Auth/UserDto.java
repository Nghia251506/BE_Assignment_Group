package com.tns.newscrawler.dto.Auth;
import lombok.Data;

import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String username;   // hoặc email tuỳ anh
    private String fullName;   // nếu entity User có
    private String status;     // ACTIVE/LOCKED... nếu có cột
    private Set<String> roles; // ROLE_ADMIN, ROLE_EDITOR...
}
