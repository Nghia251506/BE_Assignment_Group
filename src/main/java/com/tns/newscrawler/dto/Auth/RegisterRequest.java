package com.tns.newscrawler.dto.Auth;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password; // Đây là mật khẩu thô từ FE
    private String email;
    private String fullName;
    private String roleName;
}
