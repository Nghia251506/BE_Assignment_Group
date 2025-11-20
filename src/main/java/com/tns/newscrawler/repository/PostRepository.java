package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.Post;
import com.tns.newscrawler.entity.Post.DeleteStatus;
import com.tns.newscrawler.entity.Post.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // =====================
    // ORIGIN URL (crawler)
    // =====================

    Optional<Post> findByOriginUrl(String originUrl);

    boolean existsByOriginUrl(String originUrl);


    // =====================
    // ADMIN: list full (kèm tenant/source/category)
    // =====================

    @EntityGraph(attributePaths = {"tenant", "source", "category"})
    Page<Post> findAll(Pageable pageable);


    // =====================
    // DETAIL / PUBLIC BY SLUG
    // =====================

    // (ít dùng, vì multi-tenant, để lại cho admin/search nội bộ nếu cần)
    Optional<Post> findBySlug(String slug);

    // Public detail: 1 tenant + 1 slug + đã publish + chưa delete
    Optional<Post> findByTenant_IdAndSlugAndStatusAndDeleteStatus(
            Long tenantId,
            String slug,
            PostStatus status,
            DeleteStatus deleteStatus
    );


    // =====================
    // CLIENT LIST / ADMIN LIST (theo title)
    // =====================

    // List theo tenant + status + deleteStatus + keyword title
    Page<Post> findByTenant_IdAndDeleteStatusAndStatusAndTitleContainingIgnoreCase(
            Long tenantId,
            DeleteStatus deleteStatus,
            PostStatus status,
            String keyword,
            Pageable pageable
    );

    // Filter thêm category
    Page<Post> findByTenant_IdAndDeleteStatusAndStatusAndCategory_IdAndTitleContainingIgnoreCase(
            Long tenantId,
            DeleteStatus deleteStatus,
            PostStatus status,
            Long categoryId,
            String keyword,
            Pageable pageable
    );

    // Filter thêm source
    Page<Post> findByTenant_IdAndDeleteStatusAndStatusAndSource_IdAndTitleContainingIgnoreCase(
            Long tenantId,
            DeleteStatus deleteStatus,
            PostStatus status,
            Long sourceId,
            String keyword,
            Pageable pageable
    );


    // =====================
    // PUBLIC: latest / by category
    // =====================

    Page<Post> findByTenant_IdAndStatusAndDeleteStatusOrderByPublishedAtDesc(
            Long tenantId,
            PostStatus status,
            DeleteStatus deleteStatus,
            Pageable pageable
    );

    Page<Post> findByTenant_IdAndCategory_IdAndStatusAndDeleteStatusOrderByPublishedAtDesc(
            Long tenantId,
            Long categoryId,
            PostStatus status,
            DeleteStatus deleteStatus,
            Pageable pageable
    );


    // =====================
    // PENDING QUEUE cho content crawler
    // =====================

    // Lấy danh sách post pending của 1 source (phục vụ content crawler)
    Page<Post> findBySource_IdAndStatus(
            Long sourceId,
            PostStatus status,
            Pageable pageable
    );

    // Nếu muốn crawl toàn hệ thống:
    Page<Post> findByStatus(PostStatus status, Pageable pageable);


    // =====================
    // FULLTEXT SEARCH (public search)
    // =====================

    @Query(
            value = """
                    SELECT * FROM posts 
                    WHERE tenant_id = :tenantId 
                      AND status = :status 
                      AND delete_status = :deleteStatus
                      AND MATCH(title, summary, content) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM posts 
                    WHERE tenant_id = :tenantId 
                      AND status = :status 
                      AND delete_status = :deleteStatus
                      AND MATCH(title, summary, content) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
                    """,
            nativeQuery = true
    )
    Page<Post> searchFullText(
            @Param("keyword") String keyword,
            @Param("tenantId") Long tenantId,
            @Param("status") String status,
            @Param("deleteStatus") String deleteStatus,
            Pageable pageable
    );
}
