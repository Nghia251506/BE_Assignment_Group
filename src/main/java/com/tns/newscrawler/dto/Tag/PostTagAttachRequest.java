package com.tns.newscrawler.dto.Tag;
import lombok.Data;

@Data
public class PostTagAttachRequest {
    private Long postId;
    private Long tagId;
}