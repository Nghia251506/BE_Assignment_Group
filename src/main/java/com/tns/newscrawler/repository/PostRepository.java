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
    // ADMIN: list full (kèm source/category)
    // =====================
    @Query("SELECT p FROM Post p ORDER BY p.id DESC ")
//    @EntityGraph(attributePaths = {"source", "category"})
    Page<Post> findAll(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.category.slug = :slug")
    int countByCategorySlug(String slug);

    // =====================
    // DETAIL / PUBLIC BY SLUG
    // =====================
    Optional<Post> findBySlug(String slug);

    // =====================
    // CLIENT LIST / ADMIN LIST (theo title)
    // =====================
    Page<Post> findByDeleteStatusAndStatusAndTitleContainingIgnoreCase(
            DeleteStatus deleteStatus,
            PostStatus status,
            String keyword,
            Pageable pageable
    );

    // Filter thêm category
    Page<Post> findByDeleteStatusAndStatusAndCategory_IdAndTitleContainingIgnoreCase(
            DeleteStatus deleteStatus,
            PostStatus status,
            Long categoryId,
            String keyword,
            Pageable pageable
    );

    // Filter thêm source
    Page<Post> findByDeleteStatusAndStatusAndSource_IdAndTitleContainingIgnoreCase(
            DeleteStatus deleteStatus,
            PostStatus status,
            Long sourceId,
            String keyword,
            Pageable pageable
    );

    // =====================
    // PUBLIC: latest / by category
    // =====================
    Page<Post> findByStatusAndDeleteStatusOrderByPublishedAtDesc(
            PostStatus status,
            DeleteStatus deleteStatus,
            Pageable pageable
    );

    Page<Post> findByCategory_IdAndStatusAndDeleteStatusOrderByPublishedAtDesc(
            Long categoryId,
            PostStatus status,
            DeleteStatus deleteStatus,
            Pageable pageable
    );

    Optional<Post> findBySlugAndStatusAndDeleteStatus(
            String slug,
            PostStatus status,
            DeleteStatus deleteStatus
    );

    // =====================
    // PENDING QUEUE cho content crawler
    // =====================
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
                    WHERE status = :status 
                      AND delete_status = :deleteStatus
                      AND MATCH(title, summary, content) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM posts 
                    WHERE status = :status 
                      AND delete_status = :deleteStatus
                      AND MATCH(title, summary, content) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
                    """,
            nativeQuery = true
    )
    Page<Post> searchFullText(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("deleteStatus") String deleteStatus,
            Pageable pageable
    );
}
