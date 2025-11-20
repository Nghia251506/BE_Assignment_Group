package com.tns.newscrawler.service.Post;

import com.tns.newscrawler.dto.Post.PostCreateRequest;
import com.tns.newscrawler.dto.Post.PostDetailDto;
import com.tns.newscrawler.dto.Post.PostDto;
import com.tns.newscrawler.dto.Post.PostSearchRequest;
import com.tns.newscrawler.dto.Post.PostUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    // =====================
    // ADMIN / INTERNAL
    // =====================

    /**
     * Tìm kiếm / liệt kê bài viết cho admin (theo tenant, category, source, status, keyword...).
     * Dùng chung cho màn list admin.
     */
    Page<PostDto> search(PostSearchRequest req);

    /**
     * Lấy chi tiết bài theo id (admin).
     * Trong impl sẽ check tenantId nếu cần.
     */
    PostDto getById(Long id);

    /**
     * Tạo bài mới (admin/crawler).
     * - Với crawler: truyền originUrl bắt buộc, tenantId/sourceId/categoryId trong request.
     * - Với admin: có thể bỏ originUrl hoặc để null.
     */
    PostDto create(PostCreateRequest req);

    /**
     * Cập nhật bài (admin).
     * Cho phép chỉnh SEO, nội dung, category, thumbnail, status...
     */
    PostDto update(Long id, PostUpdateRequest req);

    /**
     * Publish bài: set status = published, publishedAt = now.
     */
    PostDto publishPost(Long tenantId, Long currentUserId, Long id);

    /**
     * Unpublish bài: chuyển về draft hoặc removed (tùy logic bên trong).
     */
    PostDto unpublishPost(Long tenantId, Long currentUserId, Long id);

    /**
     * Soft delete: deleteStatus = Deleted, set deletedAt / deletedBy.
     */
    void softDeletePost(Long tenantId, Long currentUserId, Long id);

    /**
     * Restore bài đã soft delete: deleteStatus = Active, clear deletedAt/deletedBy.
     */
    void restorePost(Long tenantId, Long currentUserId, Long id);


    // =====================
    // CRAWLER HELPERS
    // =====================

    /**
     * Kiểm tra đã có bài với originUrl này chưa.
     */
    boolean existsByOrigin(String originUrl);

    /**
     * Upsert theo originUrl:
     * - Nếu chưa tồn tại: create mới.
     * - Nếu đã tồn tại: update các field title/summary/contentRaw/thumbnail...
     */
    PostDto upsertByOrigin(PostCreateRequest req);

    /**
     * Placeholder cho việc "generate unique content" từ contentRaw, v.v.
     * Anh sẽ implement chi tiết sau.
     */
    PostDto generatePost(Long id);


    // =====================
    // PUBLIC API
    // =====================

    /**
     * Lấy chi tiết bài public theo tenant + slug.
     * Chỉ trả về nếu status=published & deleteStatus=Active.
     */
    PostDetailDto getPostBySlug(Long tenantId, String slug);

    /**
     * Lấy danh sách bài mới nhất cho 1 tenant (public).
     */
    Page<PostDto> getLatestPosts(Long tenantId, Pageable pageable);

    /**
     * Search public theo keyword (dùng FULLTEXT).
     * Mặc định chỉ trả bài published + Active.
     */
    Page<PostDto> searchPublic(Long tenantId, String keyword, Pageable pageable);
}
