package com.tns.newscrawler.dto.Tag;
import lombok.Data;

@Data
public class TagCreateRequest {
    private String name;
    private String slug;     // optional
}

