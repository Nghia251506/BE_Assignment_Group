package com.tns.newscrawler.dto.Tenant;

import lombok.Data;

@Data
public class TenantUpdateRequest {
    private String name;
    private String contactEmail;
    private String contactPhone;
    private Integer maxUsers;
    private String status;
}
