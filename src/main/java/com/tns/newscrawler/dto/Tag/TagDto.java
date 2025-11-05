package com.tns.newscrawler.dto.Tag;
import lombok.Data;

@Data
public class TagDto {
    private Long id;
    private Long tenantId;
    private String name;
    private String slug;
}
