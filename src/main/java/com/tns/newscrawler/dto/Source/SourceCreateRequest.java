package com.tns.newscrawler.dto.Source;

import lombok.Data;

@Data
public class SourceCreateRequest {
    private Long categoryId;
    private String name;
    private String baseUrl;
    private String listUrl;
    private String listItemSelector;
    private String linkAttr;        // có thể để null, BE default = href
    private String titleSelector;
    private String contentSelector;
    private String thumbnailSelector;
    private String authorSelector;
    private String note;
}
