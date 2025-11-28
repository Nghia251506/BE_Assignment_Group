package com.tns.newscrawler.dto.Post;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Data
@Getter
@Setter
public class PostBulkActionRequest {
    private List<Long> postIds;
    private String action;
}
