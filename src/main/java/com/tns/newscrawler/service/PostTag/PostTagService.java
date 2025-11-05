package com.tns.newscrawler.service.PostTag;

import com.tns.newscrawler.dto.Tag.TagDto;

import java.util.List;

public interface PostTagService {
    void attach(Long postId, Long tagId);
    void detach(Long postId, Long tagId);
    List<TagDto> listTagsOfPost(Long postId);
    List<Long> listPostIdsByTag(Long tagId);
}
