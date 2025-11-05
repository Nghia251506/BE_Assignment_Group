package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.Post;
import com.tns.newscrawler.entity.Post.DeleteStatus;
import com.tns.newscrawler.entity.Post.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByOriginUrl(String originUrl);

    boolean existsByOriginUrl(String originUrl);

    // client list
    Page<Post> findByTenant_IdAndDeleteStatusAndStatusAndTitleContainingIgnoreCase(
            Long tenantId, DeleteStatus ds, PostStatus st, String keyword, Pageable pageable);

    // filter thêm category/source
    Page<Post> findByTenant_IdAndDeleteStatusAndStatusAndCategory_IdAndTitleContainingIgnoreCase(
            Long tenantId, DeleteStatus ds, PostStatus st, Long categoryId, String keyword, Pageable pageable);

    Page<Post> findByTenant_IdAndDeleteStatusAndStatusAndSource_IdAndTitleContainingIgnoreCase(
            Long tenantId, DeleteStatus ds, PostStatus st, Long sourceId, String keyword, Pageable pageable);
}
