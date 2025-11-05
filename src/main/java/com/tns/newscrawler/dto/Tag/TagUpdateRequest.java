package com.tns.newscrawler.dto.Tag;
import lombok.Data;

@Data
public class TagUpdateRequest {
    private String name;
    private String slug;
}
