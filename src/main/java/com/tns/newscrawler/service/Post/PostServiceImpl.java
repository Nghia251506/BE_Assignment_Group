package com.tns.newscrawler.service.Post;

import com.tns.newscrawler.dto.Post.*;
import com.tns.newscrawler.dto.common.PageResponse;
import com.tns.newscrawler.entity.*;
import com.tns.newscrawler.entity.Post.DeleteStatus;
import com.tns.newscrawler.entity.Post.PostStatus;
import com.tns.newscrawler.mapper.Post.PostMapper;
import com.tns.newscrawler.repository.*;
import com.tns.newscrawler.service.Post.PostService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getAllPosts(Pageable pageable) {
        Page<Post> page = postRepo.findAll(pageable);
        // hoặc postRepository.findAll(pageable);

        List<PostDto> content = page.getContent()
                .stream()
                .map(PostMapper::toDto)
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    private PostDto toDto(Post p) {
        PostDto dto = new PostDto();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setSummary(p.getSummary());
        dto.setStatus(String.valueOf(p.getStatus()));
        dto.setDeleteStatus(String.valueOf(p.getDeleteStatus()));
        dto.setPublishedAt(p.getPublishedAt());
        dto.setViewCount(p.getViewCount());
        // nếu PostDto có thêm trường sourceName, categoryName... thì set tiếp ở đây
        // dto.setSourceName(p.getSource() != null ? p.getSource().getName() : null);
        // dto.setCategoryName(p.getCategory() != null ? p.getCategory().getName() : null);
        return dto;
    }

    @Override
    public PostDto getById(Long id) {
        Post p = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        return PostMapper.toDto(p);
    }

    @Override
    public Page<PostDto> search(PostSearchRequest req) {
        var pageable = toPageable(req.getPage(), req.getSize(), req.getSort());
        var status = req.getStatus()!=null ? PostStatus.valueOf(req.getStatus()) : PostStatus.published;
        var keyword = req.getKeyword()!=null ? req.getKeyword().trim() : "";
        if (req.getCategoryId()!=null) {
            return postRepo.findByTenant_IdAndDeleteStatusAndStatusAndCategory_IdAndTitleContainingIgnoreCase(
                    req.getTenantId(), DeleteStatus.Active, status, req.getCategoryId(), keyword, pageable
            ).map(PostMapper::toDto);
        } else if (req.getSourceId()!=null) {
            return postRepo.findByTenant_IdAndDeleteStatusAndStatusAndSource_IdAndTitleContainingIgnoreCase(
                    req.getTenantId(), DeleteStatus.Active, status, req.getSourceId(), keyword, pageable
            ).map(PostMapper::toDto);
        } else {
            return postRepo.findByTenant_IdAndDeleteStatusAndStatusAndTitleContainingIgnoreCase(
                    req.getTenantId(), DeleteStatus.Active, status, keyword, pageable
            ).map(PostMapper::toDto);
        }
    }

    private Pageable toPageable(Integer page, Integer size, String sort) {
        int p = page!=null && page>=0 ? page : 0;
        int s = size!=null && size>0 ? size : 10;
        Sort sortObj = Sort.by("publishedAt").descending();
        if (sort!=null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            String field = parts[0];
            boolean desc = parts.length<2 || "DESC".equalsIgnoreCase(parts[1]);
            sortObj = desc ? Sort.by(field).descending() : Sort.by(field).ascending();
        }
        return PageRequest.of(p, s, sortObj);
    }

    @Override
    public PostDto create(PostCreateRequest req) {
        if (postRepo.existsByOriginUrl(req.getOriginUrl()))
            throw new RuntimeException("Post with origin already exists");

        Tenant tenant = tenantRepo.findById(req.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Source source = req.getSourceId()!=null
                ? sourceRepo.findById(req.getSourceId()).orElse(null) : null;
        Category category = req.getCategoryId()!=null
                ? categoryRepo.findById(req.getCategoryId()).orElse(null) : null;

        Post p = Post.builder()
                .tenant(tenant)
                .source(source)
                .category(category)
                .originUrl(req.getOriginUrl())
                .title(req.getTitle())
                .slug(req.getSlug()!=null ? req.getSlug() : slugify(req.getTitle()))
                .summary(req.getSummary())
                .content(req.getContent())
                .contentRaw(req.getContentRaw())
                .thumbnail(req.getThumbnail())
                .status(Post.PostStatus.pending)
                .deleteStatus(Post.DeleteStatus.Active)
                .build();
        postRepo.save(p);
        return PostMapper.toDto(p);
    }

    @Override
    public PostDto update(Long id, PostUpdateRequest req) {
        Post p = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        if (req.getCategoryId()!=null) {
            Category c = categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            p.setCategory(c);
        }
        if (req.getTitle()!=null) p.setTitle(req.getTitle());
        if (req.getSlug()!=null) p.setSlug(req.getSlug().isBlank() && req.getTitle()!=null
                ? slugify(req.getTitle()) : req.getSlug());
        if (req.getSummary()!=null) p.setSummary(req.getSummary());
        if (req.getContent()!=null) p.setContent(req.getContent());
        if (req.getThumbnail()!=null) p.setThumbnail(req.getThumbnail());
        if (req.getStatus()!=null) p.setStatus(PostStatus.valueOf(req.getStatus()));
        return PostMapper.toDto(p);
    }

    @Override
    public PostDto publish(Long id) {
        Post p = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        p.setStatus(PostStatus.published);
        p.setPublishedAt(LocalDateTime.now());
        return PostMapper.toDto(p);
    }

    @Override
    public void softDelete(Long id, Long userId) {
        Post p = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        p.setDeleteStatus(DeleteStatus.Deleted);
        p.setDeletedAt(LocalDateTime.now());
        p.setDeletedBy(userId);
    }

    @Override
    public void restore(Long id) {
        Post p = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        p.setDeleteStatus(DeleteStatus.Active);
        p.setDeletedAt(null);
        p.setDeletedBy(null);
    }

    @Override
    public boolean existsByOrigin(String originUrl) {
        return postRepo.existsByOriginUrl(originUrl);
    }

    @Override
    public PostDto upsertByOrigin(PostCreateRequest req) {
        Post p = postRepo.findByOriginUrl(req.getOriginUrl()).orElse(null);
        if (p == null) return create(req);
        // update nhẹ (giữ published nếu đã publish)
        if (req.getTitle()!=null) p.setTitle(req.getTitle());
        if (req.getSlug()!=null) p.setSlug(req.getSlug());
        if (req.getSummary()!=null) p.setSummary(req.getSummary());
        if (req.getContent()!=null) p.setContent(req.getContent());
        if (req.getContentRaw()!=null) p.setContentRaw(req.getContentRaw());
        if (req.getThumbnail()!=null) p.setThumbnail(req.getThumbnail());
        if (req.getCategoryId()!=null) {
            var c = categoryRepo.findById(req.getCategoryId()).orElse(null);
            p.setCategory(c);
        }
        return PostMapper.toDto(p);
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
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");
        return s.length()>120 ? s.substring(0,120) : s;
    }
}
