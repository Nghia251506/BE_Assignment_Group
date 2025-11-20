package com.tns.newscrawler.service.Post;

import com.tns.newscrawler.dto.Post.*;
import com.tns.newscrawler.entity.Category;
import com.tns.newscrawler.entity.Post;
import com.tns.newscrawler.entity.Post.DeleteStatus;
import com.tns.newscrawler.entity.Post.PostStatus;
import com.tns.newscrawler.entity.Source;
import com.tns.newscrawler.entity.Tenant;
import com.tns.newscrawler.mapper.Post.PostMapper;
import com.tns.newscrawler.repository.*;
import org.jsoup.Jsoup;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepo;
    private final TenantRepository tenantRepo;
    private final SourceRepository sourceRepo;
    private final CategoryRepository categoryRepo;

    public PostServiceImpl(PostRepository postRepo,
                           TenantRepository tenantRepo,
                           SourceRepository sourceRepo,
                           CategoryRepository categoryRepo) {
        this.postRepo = postRepo;
        this.tenantRepo = tenantRepo;
        this.sourceRepo = sourceRepo;
        this.categoryRepo = categoryRepo;
    }

    // ==========================
    // PUBLIC API (for client)
    // ==========================

    @Override
    @Transactional(readOnly = true)
    public PostDetailDto getPostBySlug(Long tenantId, String slug) {
        Post post = postRepo.findByTenant_IdAndSlugAndStatusAndDeleteStatus(
                        tenantId, slug, PostStatus.published, DeleteStatus.Active)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return PostMapper.toDetailDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getLatestPosts(Long tenantId, Pageable pageable) {
        Page<Post> page = postRepo.findByTenant_IdAndStatusAndDeleteStatusOrderByPublishedAtDesc(
                tenantId, PostStatus.published, DeleteStatus.Active, pageable);
        return page.map(PostMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> searchPublic(Long tenantId, String keyword, Pageable pageable) {
        return postRepo.searchFullText(keyword, tenantId, PostStatus.published.name(), DeleteStatus.Active.name(), pageable)
                .map(PostMapper::toDto);
    }

    // ==========================
    // ADMIN CRUD (for admin)
    // ==========================

    @Override
    public PostDto unpublishPost(Long tenantId, Long currentUserId, Long id) {
        Post post = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setStatus(PostStatus.draft);  // hoặc chuyển sang PostStatus.removed tùy vào logic
        post.setUpdatedAt(LocalDateTime.now());

        Post savedPost = postRepo.save(post);
        return PostMapper.toDto(savedPost);
    }

    @Override
    public Page<PostDto> search(PostSearchRequest req) {
        Pageable pageable = toPageable(req.getPage(), req.getSize(), req.getSort());
        PostStatus status = req.getStatus() != null ? PostStatus.valueOf(req.getStatus()) : PostStatus.published;
        String keyword = req.getKeyword() != null ? req.getKeyword().trim() : "";

        if (req.getCategoryId() != null) {
            return postRepo.findByTenant_IdAndDeleteStatusAndStatusAndCategory_IdAndTitleContainingIgnoreCase(
                            req.getTenantId(), DeleteStatus.Active, status, req.getCategoryId(), keyword, pageable)
                    .map(PostMapper::toDto);
        } else if (req.getSourceId() != null) {
            return postRepo.findByTenant_IdAndDeleteStatusAndStatusAndSource_IdAndTitleContainingIgnoreCase(
                            req.getTenantId(), DeleteStatus.Active, status, req.getSourceId(), keyword, pageable)
                    .map(PostMapper::toDto);
        } else {
            return postRepo.findByTenant_IdAndDeleteStatusAndStatusAndTitleContainingIgnoreCase(
                            req.getTenantId(), DeleteStatus.Active, status, keyword, pageable)
                    .map(PostMapper::toDto);
        }
    }

    @Override
    public PostDto getById(Long id) {
        Post post = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        return PostMapper.toDto(post);
    }

    @Override
    public PostDto create(PostCreateRequest req) {
        if (postRepo.existsByOriginUrl(req.getOriginUrl())) {
            throw new RuntimeException("Post with origin already exists");
        }

        Tenant tenant = tenantRepo.findById(req.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Source source = req.getSourceId() != null
                ? sourceRepo.findById(req.getSourceId()).orElse(null) : null;
        Category category = req.getCategoryId() != null
                ? categoryRepo.findById(req.getCategoryId()).orElse(null) : null;

        Post post = Post.builder()
                .tenant(tenant)
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

    @Override
    public PostDto update(Long id, PostUpdateRequest req) {
        Post post = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));

        if (req.getCategoryId() != null) {
            Category category = categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            post.setCategory(category);
        }

        if (req.getTitle() != null) post.setTitle(req.getTitle());
        if (req.getSlug() != null) post.setSlug(req.getSlug().isBlank() && req.getTitle() != null
                ? slugify(req.getTitle()) : req.getSlug());
        if (req.getSummary() != null) post.setSummary(req.getSummary());
        if (req.getContent() != null) post.setContent(req.getContent());
        if (req.getThumbnail() != null) post.setThumbnail(req.getThumbnail());
        if (req.getStatus() != null) post.setStatus(PostStatus.valueOf(req.getStatus()));

        Post savedPost = postRepo.save(post);
        return PostMapper.toDto(savedPost);
    }

    @Override
    public PostDto publishPost(Long tenantId, Long currentUserId, Long id) {
        Post post = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setStatus(PostStatus.published);
        post.setPublishedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        Post savedPost = postRepo.save(post);
        return PostMapper.toDto(savedPost);
    }

    @Override
    public void softDeletePost(Long tenantId, Long currentUserId, Long id) {
        Post post = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setDeleteStatus(DeleteStatus.Deleted);
        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(currentUserId);

        postRepo.save(post);
    }

    @Override
    public void restorePost(Long tenantId, Long currentUserId, Long id) {
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

    @Override
    public PostDto generatePost(Long id) {
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
            if (text.length() > 300) {
                post.setSummary(text.substring(0, 300) + "...");
            } else {
                post.setSummary(text);
            }
            changed = true;
        }

        if (!StringUtils.hasText(post.getTitle()) && StringUtils.hasText(post.getSummary())) {
            String s = post.getSummary();
            if (s.length() > 80) {
                post.setTitle(s.substring(0, 80) + "...");
            } else {
                post.setTitle(s);
            }
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

        if (changed) post.setUpdatedAt(LocalDateTime.now());

        Post savedPost = postRepo.save(post);
        return PostMapper.toDto(savedPost);
    }

    // util: slugify đơn giản
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
        Sort sortObj = Sort.by("publishedAt").descending();  // mặc định sắp xếp theo publishedAt DESC
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            String field = parts[0];
            boolean desc = parts.length < 2 || "DESC".equalsIgnoreCase(parts[1]);
            sortObj = desc ? Sort.by(field).descending() : Sort.by(field).ascending();
        }
        return PageRequest.of(p, s, sortObj);
    }
}
