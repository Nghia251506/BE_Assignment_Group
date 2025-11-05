package com.tns.newscrawler.dto.Tenant;

import lombok.Data;

@Data
public class TenantCreateRequest {
    private String code;
    private String name;
    private String contactEmail;
    private String contactPhone;
    private Integer maxUsers;
}
