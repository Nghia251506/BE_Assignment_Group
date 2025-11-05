package com.tns.newscrawler.dto.Tenant;

import lombok.Data;

@Data
public class TenantDto {
    private Long id;
    private String code;
    private String name;
    private String contactEmail;
    private String contactPhone;
    private Integer maxUsers;
    private String status;
}
