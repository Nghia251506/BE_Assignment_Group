package com.tns.newscrawler.repository;

import com.tns.newscrawler.dto.Post.PostDto;
import com.tns.newscrawler.entity.Post;
import com.tns.newscrawler.entity.Post.DeleteStatus;
import com.tns.newscrawler.entity.Post.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // =====================
    // ORIGIN URL (crawler)
    // =====================
    Optional<Post> findByOriginUrl(String originUrl);
    //search redis
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.source")
    List<Post> findAllWithCategoryAndSource();

    boolean existsByOriginUrl(String originUrl);
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);
    @Query("SELECT p FROM Post p ORDER BY p.viewCount DESC")
    List<Post> findTop10ByViewCount(Pageable pageable);

    // =====================
    // ADMIN: list full (kèm source/category)
    // =====================
    @Query("SELECT p FROM Post p ORDER BY p.id DESC ")
//    @EntityGraph(attributePaths = {"source", "category"})
    Page<Post> findAll(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.category.slug = :slug")
    int countByCategorySlug(String slug);
    @Query("SELECT COUNT(p) FROM Post p WHERE p.source.id = :id")
    int countBySourceId(Long id);

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
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findAllByDesc();
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

    @Query("SELECT p FROM Post p WHERE LOWER(p.category.slug) = LOWER(:categorySlug) AND p.status = :status")
    Page<Post> findByCategorySlugAndStatus(
            @Param("categorySlug") String categorySlug,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId OR p.category.parentId = :parentId")
    List<Post> findByCategoryIdOrParentId(@Param("categoryId") Long categoryId, @Param("parentId") Long parentId);
    @Modifying
    @Query("DELETE FROM PostTag pt WHERE pt.post.id = :postId")
    void deleteAllTagsByPostId(@Param("postId") Long postId);
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.source " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.deleteStatus = 'Active' " +
            "ORDER BY p.createdAt DESC")
    List<Post> findTop5Recent(Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.status = :status " +
            "WHERE p.id IN :ids AND p.deleteStatus = :deleteStatus")
    void updateStatusByIds(@Param("ids") List<Long> ids,
                           @Param("status") Post.PostStatus status,
                           @Param("deleteStatus") Post.DeleteStatus deleteStatus);

    // DEFAULT METHOD – SIÊU TIỆN, KHÔNG CẦN GỌI PageRequest.of MỖI LẦN
    default List<Post> getTop5Recent() {
        return findTop5Recent(PageRequest.of(0, 5));
    }

    //count
    @Query("SELECT COUNT(p) FROM Post p WHERE p.deleteStatus = 'Active'")
    Long CountPosts();

    @Query("SELECT p FROM Post p WHERE p.deleteStatus = 'Active' ORDER BY p.createdAt DESC")
    Page<Post> findAllActivePublished(Pageable pageable);
}
