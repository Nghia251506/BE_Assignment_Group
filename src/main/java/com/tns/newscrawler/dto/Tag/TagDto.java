package com.tns.newscrawler.dto.Tag;
import com.tns.newscrawler.entity.Tenant;
import lombok.Data;

@Data
public class TagDto {
    private Long id;
    private Long tenantId;
    private Tenant tenant;
    private String name;
    private String slug;
    private String seoTitle;
    private String seoDescription;
    private String seoKeywords;
}
