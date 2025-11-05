package com.tns.newscrawler.dto.Tag;
import lombok.Data;

@Data
public class TagCreateRequest {
    private Long tenantId;   // bắt buộc
    private String name;
    private String slug;     // optional
}

