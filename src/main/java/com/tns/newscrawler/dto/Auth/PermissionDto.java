package com.tns.newscrawler.dto.Auth;

import lombok.Data;

@Data
public class PermissionDto {
    private Long id;
    private String code;
    private String name;
    private String description;
}
