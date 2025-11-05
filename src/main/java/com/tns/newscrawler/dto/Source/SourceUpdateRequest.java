package com.tns.newscrawler.dto.Source;

import lombok.Data;

@Data
public class SourceUpdateRequest {
    private Long categoryId;        // cho phép đổi category
    private String name;
    private String baseUrl;
    private String listUrl;
    private String listItemSelector;
    private String linkAttr;
    private String titleSelector;
    private String contentSelector;
    private String thumbnailSelector;
    private String authorSelector;
    private Boolean isActive;
    private String note;
}
