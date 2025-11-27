package com.tns.newscrawler.service.Post;

import ch.qos.logback.classic.Logger;
import com.tns.newscrawler.dto.Post.*;
import com.tns.newscrawler.entity.Category;
import com.tns.newscrawler.entity.Post;
import com.tns.newscrawler.entity.Post.DeleteStatus;
import com.tns.newscrawler.entity.Post.PostStatus;
import com.tns.newscrawler.entity.Source;
import com.tns.newscrawler.mapper.Post.PostMapper;
import com.tns.newscrawler.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jsoup.Jsoup;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepo;
    private final SourceRepository sourceRepo;
    private final CategoryRepository categoryRepo;
    @PersistenceContext
    private EntityManager entityManager;

    public PostServiceImpl(PostRepository postRepo, SourceRepository sourceRepo, CategoryRepository categoryRepo) {
        this.postRepo = postRepo;
        this.sourceRepo = sourceRepo;
        this.categoryRepo = categoryRepo;
    }

    // ==========================
    // PUBLIC API (for client)
    // ==========================


    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getLatestPosts(Pageable pageable) {
        Page<Post> page = postRepo.findByStatusAndDeleteStatusOrderByPublishedAtDesc(
                PostStatus.published, DeleteStatus.Active, pageable);
        return page.map(PostMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> searchPublic(String keyword, Pageable pageable) {
        return postRepo.searchFullText(keyword, PostStatus.published.name(), DeleteStatus.Active.name(), pageable)
                .map(PostMapper::toDto);
    }
    @Override
    public List<PostDto> getPostsByCategory(Long categoryId, Long parentId) {
        List<Post> posts = postRepo.findByCategoryIdOrParentId(categoryId, parentId);
        return posts.stream().map(PostMapper::toDto).collect(Collectors.toList());
    }

    // ==========================
    // ADMIN CRUD (for admin)
    // ==========================

    @Override
    public PostDto unpublishPost(Long currentUserId, Long id) {
        Post post = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setStatus(PostStatus.draft);
        post.setUpdatedAt(LocalDateTime.now());

        Post savedPost = postRepo.save(post);
        return PostMapper.toDto(savedPost);
    }
    @Override
    public List<PostDto> findAllByDesc(){
        List<Post> posts = postRepo.findAllByDesc();
        return posts.stream().map(PostMapper::toDto).collect(Collectors.toList());
    }
    @Override
    public void increaseViewCount(Long id) {
        postRepo.incrementViewCount(id);
    }
    @Override
    public void increaseViewCountBySlug(String slug) {
        postRepo.findBySlug(slug)
                .ifPresent(post -> postRepo.incrementViewCount(post.getId()));
    }
    @Override
    public int getArticleCountByCategorySlug(String categorySlug) {
        return postRepo.countByCategorySlug(categorySlug);
    }
    @Override
    public int getArticleCountBySourceId(Long SourceId) {
        return postRepo.countBySourceId(SourceId);
    }

    @Override
    public Page<PostDto> search(PostSearchRequest req) {
        Pageable pageable = toPageable(req.getPage(), req.getSize(), req.getSort());
        PostStatus status = req.getStatus() != null ? PostStatus.valueOf(req.getStatus()) : PostStatus.published;
        String keyword = req.getKeyword() != null ? req.getKeyword().trim() : "";
        if ((req.getCategoryId() == null || req.getCategoryId() == 0)
                && (req.getSourceId() == null || req.getSourceId() == 0)
                && keyword.isEmpty()) {

            return postRepo.findAllActivePublished(pageable)
                    .map(PostMapper::toDto);
        }
        else if (req.getCategoryId() == null && req.getSourceId() == null && keyword.isEmpty()) {
            // Gọi findAll() nếu không có điều kiện tìm kiếm nào
            List<Post> posts = postRepo.findAll(); // Lấy tất cả bài viết
            // Tạo Page từ List
            return new PageImpl<>(posts.stream().map(PostMapper::toDto).toList(), pageable, posts.size());
        }
        else if (req.getCategoryId() != null) {
            return postRepo.findByDeleteStatusAndStatusAndCategory_IdAndTitleContainingIgnoreCase(
                            DeleteStatus.Active, status, req.getCategoryId(), keyword, pageable)
                    .map(PostMapper::toDto);
        } else if (req.getSourceId() != null) {
            return postRepo.findByDeleteStatusAndStatusAndSource_IdAndTitleContainingIgnoreCase(
                            DeleteStatus.Active, status, req.getSourceId(), keyword, pageable)
                    .map(PostMapper::toDto);
        } else {
            return postRepo.findByDeleteStatusAndStatusAndTitleContainingIgnoreCase(
                            DeleteStatus.Active, status, keyword, pageable)
                    .map(PostMapper::toDto);
        }
    }

    @Transactional
    public void bulkPublish(List<Long> postIds) {
        postRepo.updateStatusByIds(postIds, PostStatus.published, DeleteStatus.Active);
    }

//    @Transactional
//    public void bulkGenerateAndPublish(List<Long> postIds) {
//        List<Post> posts = postRepo.findAllById(postIds);
//        for (Post post : posts) {
//            // Gọi hàm generate của anh (có thể reuse từ crawler hoặc AI generate)
//            String generatedContent = generateService.generateFullContent(post);
//            post.setContent(generatedContent);
//            post.setContentRaw(generatedContent); // nếu cần
//            post.setStatus(PostStatus.published);
//            post.setPublishedAt(LocalDateTime.now());
//        }
//        postRepo.saveAll(posts);
//    }

    @Override
    public PostDto getById(Long id) {
        Post post = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        return PostMapper.toDto(post);
    }

    @Override
    public Long countPosts() {
        return  postRepo.CountPosts();
    }

    @Override
    public PostDto create(PostCreateRequest req) {
        if (postRepo.existsByOriginUrl(req.getOriginUrl())) {
            throw new RuntimeException("Post with origin already exists");
        }

        Source source = req.getSourceId() != null
                ? sourceRepo.findById(req.getSourceId()).orElse(null) : null;
        Category category = req.getCategoryId() != null
                ? categoryRepo.findById(req.getCategoryId()).orElse(null) : null;

        Post post = Post.builder()
                .source(source)
                .category(category)
                .originUrl(req.getOriginUrl())
                .title(req.getTitle())
                .slug(req.getSlug() != null ? req.getSlug() : slugify(req.getTitle()))
                .summary(req.getSummary())
                .content(req.getContent())
                .contentRaw(req.getContentRaw())
                .thumbnail(req.getThumbnail())
                .status(PostStatus.pending)
                .deleteStatus(DeleteStatus.Active)
                .build();

        postRepo.save(post);
        return PostMapper.toDto(post);
    }

    @Transactional
    @Override
    public PostDto update(Long id, PostUpdateRequest request) {
        // 1. LẤY ENTITY CHỨ KHÔNG PHẢI DTO!!!
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        // 2. CẬP NHẬT CÁC FIELD TỪ REQUEST VÀO ENTITY
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setThumbnail(request.getThumbnail());
        post.setSeoTitle(request.getSeoTitle());
        post.setSeoDescription(request.getSeoDescription());
        post.setSeoKeywords(request.getSeoKeywords());

        // Nếu có status
        if (request.getStatus() != null) {
            post.setStatus(PostStatus.valueOf(request.getStatus()));
        }

        // Nếu có categoryId
        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            post.setCategory(category);
        }

        // 3. XỬ LÝ TAG – ĐÃ SỬA ĐÚNG ENTITYMANAGER!!!
        if (request.getTagIds() != null) {
            // Xóa hết tag cũ
            postRepo.deleteAllTagsByPostId(id);

            // Thêm tag mới
            if (!request.getTagIds().isEmpty()) {
                String placeholders = request.getTagIds().stream()
                        .map(tagId -> "(" + id + ", " + tagId + ")")
                        .collect(Collectors.joining(", "));

                String sql = "INSERT INTO post_tags (post_id, tag_id) VALUES " + placeholders;
                entityManager.createNativeQuery(sql).executeUpdate();
            }
        }

        // 4. SAVE ENTITY (không phải DTO!!!)
        Post updatedPost = postRepo.save(post);

        // 5. TRẢ VỀ DTO DÙNG MAPPER CỦA ANH
        return PostMapper.toDto(updatedPost);
    }

    @Override
    public PostDto publishPost(Long currentUserId) {
        Post post = postRepo.findById(currentUserId).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setStatus(PostStatus.published);
        post.setPublishedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        Post savedPost = postRepo.save(post);
        return PostMapper.toDto(savedPost);
    }

    @Override
    public void softDeletePost(Long currentUserId, Long id) {
        Post post = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setDeleteStatus(DeleteStatus.Deleted);
        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(currentUserId);

        postRepo.save(post);
    }

    @Override
    public void restorePost(Long currentUserId, Long id) {
        Post post = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setDeleteStatus(DeleteStatus.Active);
        post.setDeletedAt(null);
        post.setDeletedBy(null);

        postRepo.save(post);
    }

    // ==========================
    // CRAWLER HELPERS
    // ==========================

    @Override
    public boolean existsByOrigin(String originUrl) {
        return postRepo.existsByOriginUrl(originUrl);
    }

    @Override
    public PostDto upsertByOrigin(PostCreateRequest req) {
        Post post = postRepo.findByOriginUrl(req.getOriginUrl()).orElse(null);

        if (post == null) return create(req);

        // Update fields (preserve published status)
        if (req.getTitle() != null) post.setTitle(req.getTitle());
        if (req.getSlug() != null) post.setSlug(req.getSlug());
        if (req.getSummary() != null) post.setSummary(req.getSummary());
        if (req.getContent() != null) post.setContent(req.getContent());
        if (req.getContentRaw() != null) post.setContentRaw(req.getContentRaw());
        if (req.getThumbnail() != null) post.setThumbnail(req.getThumbnail());
        if (req.getCategoryId() != null) {
            Category category = categoryRepo.findById(req.getCategoryId()).orElse(null);
            post.setCategory(category);
        }

        Post savedPost = postRepo.save(post);
        return PostMapper.toDto(savedPost);
    }

    @Transactional
    public Post generatePost(Long id) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));

        boolean changed = false;

        if (!StringUtils.hasText(post.getContent()) && StringUtils.hasText(post.getContentRaw())) {
            String text = Jsoup.parse(post.getContentRaw()).text();
            post.setContent(text);
            changed = true;
        }

        if (!StringUtils.hasText(post.getSummary()) && StringUtils.hasText(post.getContent())) {
            String text = post.getContent();
            post.setSummary(text.length() > 300 ? text.substring(0, 300) + "..." : text);
            changed = true;
        }

        if (!StringUtils.hasText(post.getTitle()) && StringUtils.hasText(post.getSummary())) {
            String title = post.getSummary().length() > 80
                    ? post.getSummary().substring(0, 80) + "..."
                    : post.getSummary();
            post.setTitle(title);
            changed = true;
        }

        if (!StringUtils.hasText(post.getSlug()) && StringUtils.hasText(post.getTitle())) {
            post.setSlug(slugify(post.getTitle()));
            changed = true;
        }

        if (post.getStatus() == null || post.getStatus() == PostStatus.pending) {
            post.setStatus(PostStatus.draft);
            changed = true;
        }

        if (changed) {
            post.setUpdatedAt(LocalDateTime.now());
        }

        return postRepo.save(post); // ← TRẢ VỀ ENTITY ĐÃ SAVE
    }

    @Transactional
    public void bulkGenerateAndPublish(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return;

        List<Post> updatedPosts = new ArrayList<>();

        for (Long id : postIds) {
            try {
                Post post = generatePost(id);  // ← REUSE 100% LOGIC
                post.setStatus(PostStatus.published);
                post.setPublishedAt(LocalDateTime.now());
                post.setUpdatedAt(LocalDateTime.now());
                updatedPosts.add(post);
            } catch (Exception e) {
                // Log lỗi nhưng không làm hỏng cả batch
                Logger log = null;
                log.warn("Không thể generate bài viết ID: " + id, e);
            }
        }

        postRepo.saveAll(updatedPosts);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailDto getPostBySlug(String slug) {
        Post post = postRepo.findBySlugAndStatusAndDeleteStatus(
                        slug, PostStatus.published, DeleteStatus.Active)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return PostMapper.toDetailDto(post);
    }


    private String slugify(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase()
                .replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a")
                .replaceAll("[éèẻẽẹêếềểễệ]", "e")
                .replaceAll("[íìỉĩị]", "i")
                .replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o")
                .replaceAll("[úùủũụưứừửữự]", "u")
                .replaceAll("[ýỳỷỹỵ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z09\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");
        return s.length() > 120 ? s.substring(0, 120) : s;
    }

    private Pageable toPageable(Integer page, Integer size, String sort) {
        int p = page != null && page >= 0 ? page : 0;
        int s = size != null && size > 0 ? size : 10;
        Sort sortObj = Sort.by("publishedAt").descending();
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            String field = parts[0];
            boolean desc = parts.length < 2 || "DESC".equalsIgnoreCase(parts[1]);
            sortObj = desc ? Sort.by(field).descending() : Sort.by(field).ascending();
        }
        return PageRequest.of(p, s, sortObj);
    }
}
