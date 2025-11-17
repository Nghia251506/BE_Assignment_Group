package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.Post;
import com.tns.newscrawler.entity.Post.DeleteStatus;
import com.tns.newscrawler.entity.Post.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByOriginUrl(String originUrl);

    boolean existsByOriginUrl(String originUrl);
    @EntityGraph(attributePaths = {"tenant", "source", "category"})
    Page<Post> findAll(Pageable pageable);
    Optional<Post> findBySlug(String slug);
    // client list
    Page<Post> findByTenant_IdAndDeleteStatusAndStatusAndTitleContainingIgnoreCase(
            Long tenantId, DeleteStatus ds, PostStatus st, String keyword, Pageable pageable);

    // filter thêm category/source
    Page<Post> findByTenant_IdAndDeleteStatusAndStatusAndCategory_IdAndTitleContainingIgnoreCase(
            Long tenantId, DeleteStatus ds, PostStatus st, Long categoryId, String keyword, Pageable pageable);

    Page<Post> findByTenant_IdAndDeleteStatusAndStatusAndSource_IdAndTitleContainingIgnoreCase(
            Long tenantId, DeleteStatus ds, PostStatus st, Long sourceId, String keyword, Pageable pageable);
    // Lấy danh sách post pending của 1 source (phục vụ content crawler)
    Page<Post> findBySource_IdAndStatus(Long sourceId, Post.PostStatus status, Pageable pageable);

    // Nếu muốn crawl toàn hệ thống:
    Page<Post> findByStatus(Post.PostStatus status, Pageable pageable);
}
