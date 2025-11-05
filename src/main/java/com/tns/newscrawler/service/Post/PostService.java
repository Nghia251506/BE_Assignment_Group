package com.tns.newscrawler.service.Post;

import com.tns.newscrawler.dto.Post.PostCreateRequest;
import com.tns.newscrawler.dto.Post.PostDto;
import com.tns.newscrawler.dto.Post.PostSearchRequest;
import com.tns.newscrawler.dto.Post.PostUpdateRequest;
import org.springframework.data.domain.Page;

public interface PostService {
    PostDto getById(Long id);
    Page<PostDto> search(PostSearchRequest req);

    PostDto create(PostCreateRequest req);         // admin/crawler upsert theo origin_url
    PostDto update(Long id, PostUpdateRequest req);

    PostDto publish(Long id);                      // set status=published + publishedAt=now
    void softDelete(Long id, Long userId);         // deleteStatus=DELETED + deletedAt/by
    void restore(Long id);                         // ACTIVE lại

    // Crawler helpers
    boolean existsByOrigin(String originUrl);
    PostDto upsertByOrigin(PostCreateRequest req); // nếu tồn tại -> update fields mới
}
