package com.tns.newscrawler.dto.User;

import lombok.Data;

@Data
public class UserCreateRequest {
    private Long tenantId;      // bắt buộc vì mình tạo user theo tenant
    private String username;
    private String password;    // sẽ hash lưu vào password_hash
    private String fullName;
    private String email;
    private String role;        // ADMIN / EDITOR / VIEWER
}
