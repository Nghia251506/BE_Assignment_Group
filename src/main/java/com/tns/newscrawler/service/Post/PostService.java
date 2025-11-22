package com.tns.newscrawler.service.Post;

import com.tns.newscrawler.dto.Post.PostCreateRequest;
import com.tns.newscrawler.dto.Post.PostDetailDto;
import com.tns.newscrawler.dto.Post.PostDto;
import com.tns.newscrawler.dto.Post.PostSearchRequest;
import com.tns.newscrawler.dto.Post.PostUpdateRequest;
import com.tns.newscrawler.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {

    // =====================
    // ADMIN / INTERNAL
    // =====================

    Page<PostDto> search(PostSearchRequest req);
    int getArticleCountByCategorySlug(String categorySlug);
    int getArticleCountBySourceId(Long SourceId);
    PostDto getById(Long id);

    PostDto create(PostCreateRequest req);

    PostDto update(Long id, PostUpdateRequest req);

    PostDto publishPost(Long currentUserId);

    PostDto unpublishPost(Long currentUserId, Long id);

    void softDeletePost(Long currentUserId, Long id);

    void restorePost(Long currentUserId, Long id);

    boolean existsByOrigin(String originUrl);

    PostDto upsertByOrigin(PostCreateRequest req);

    PostDto generatePost(Long id);

    List<PostDto> getPostsByCategory(Long categoryId, Long parentId);

    // =====================
    // PUBLIC API
    // =====================

    PostDetailDto getPostBySlug(String slug);

    Page<PostDto> getLatestPosts(Pageable pageable);

    Page<PostDto> searchPublic(String keyword, Pageable pageable);
}
