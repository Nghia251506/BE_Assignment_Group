package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.Category.CategoryCreateRequest;
import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Category.CategoryUpdateRequest;
import com.tns.newscrawler.dto.Post.PostCreateRequest;
import com.tns.newscrawler.dto.Post.PostDto;
import com.tns.newscrawler.dto.Post.PostUpdateRequest;
import com.tns.newscrawler.dto.Source.SourceCreateRequest;
import com.tns.newscrawler.dto.Source.SourceDto;
import com.tns.newscrawler.dto.Source.SourceUpdateRequest;
import com.tns.newscrawler.dto.Tenant.TenantCreateRequest;
import com.tns.newscrawler.dto.Tenant.TenantDto;
import com.tns.newscrawler.dto.Tenant.TenantUpdateRequest;
import com.tns.newscrawler.dto.User.UserCreateRequest;
import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.User.UserUpdateRequest;
import com.tns.newscrawler.service.Category.CategoryService;
import com.tns.newscrawler.service.Crawler.ContentCrawlerService;
import com.tns.newscrawler.service.Crawler.LinkCrawlerService;
import com.tns.newscrawler.service.Post.PostService;
import com.tns.newscrawler.service.Source.SourceService;
import com.tns.newscrawler.service.Tenant.TenantService;
import com.tns.newscrawler.service.User.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CategoryService categoryService;
    private final LinkCrawlerService linkCrawlerService;
    private final ContentCrawlerService contentCrawlerService;
    private final PostService postService;
    private final SourceService sourceService;
    private final UserService userService;
    private final TenantService tenantService;

    public AdminController(CategoryService categoryService,
                           LinkCrawlerService linkCrawlerService,
                           ContentCrawlerService contentCrawlerService,
                           PostService postService, SourceService sourceService,
                           UserService userService,
                           TenantService tenantService)
    {
        this.categoryService = categoryService;
        this.linkCrawlerService = linkCrawlerService;
        this.contentCrawlerService = contentCrawlerService;
        this.postService = postService;
        this.sourceService = sourceService;
        this.userService = userService;
        this.tenantService = tenantService;
    }

    // list cho 1 tenant
    @GetMapping("/categories/tenant/{tenantId}")
    public ResponseEntity<List<CategoryDto>> getCategoryByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(categoryService.getByTenant(tenantId));
    }


    @GetMapping("/categories/{slug}")
    public ResponseEntity<List<CategoryDto>> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
    }


    @GetMapping("/categories/tenant/{tenantId}/active")
    public ResponseEntity<List<CategoryDto>> getCategoryActiveByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(categoryService.getActiveByTenant(tenantId));
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryCreateRequest req) {
        return ResponseEntity.ok(categoryService.create(req));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id,
                                              @RequestBody CategoryUpdateRequest req) {
        return ResponseEntity.ok(categoryService.update(id, req));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    //crawler
    // Crawl tất cả source active
    @PostMapping("/crawler/links/all")
    public String crawlAll() {
        int total = linkCrawlerService.crawlAllActiveSources();
        return "Upsert " + total + " links for all active sources";
    }

    // Crawl theo tenant
    @PostMapping("/crawler/links/by-tenant")
    public String crawlByTenant(@RequestParam("tenantId") Long tenantId) {
        int total = linkCrawlerService.crawlActiveSourcesByTenant(tenantId);
        return "Upsert " + total + " links for tenant " + tenantId + ": " + total + " links";
    }
    // ✅ NEW: Crawl content cho các post pending của 1 source
    @PostMapping("/crawler/content/by-source")
    public String crawlContentBySource(
            @RequestParam Long sourceId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        int ok = contentCrawlerService.crawlPendingBySource(sourceId, limit);
        return "Crawled content for " + ok + " posts of source " + sourceId;
    }

    //post
    // Get all posts for admin with pagination
    @GetMapping("/posts")
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostDto> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    // Get post by slug for admin
    @GetMapping("/posts/{slug}")
    public ResponseEntity<PostDto> getPostBySlug(@PathVariable String slug) {
        PostDto postDto = postService.getBySlug(slug);
        return ResponseEntity.ok(postDto);
    }

    // Create new post
    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(@RequestBody PostCreateRequest req) {
        PostDto postDto = postService.create(req);
        return ResponseEntity.ok(postDto);
    }

    // Update post by id
    @PutMapping("/posts/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @RequestBody PostUpdateRequest req) {
        PostDto postDto = postService.update(id, req);
        return ResponseEntity.ok(postDto);
    }

    // Publish post by id
    @PutMapping("/posts/{id}/publish")
    public ResponseEntity<PostDto> publishPost(@PathVariable Long id) {
        PostDto postDto = postService.publishPost(id);
        return ResponseEntity.ok(postDto);
    }

    // Soft delete post by id
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> softDeletePost(Long tenantId,@PathVariable Long id, @RequestParam(required = false) Long userId) {

        postService.softDeletePost(tenantId,id, userId);
        return ResponseEntity.noContent().build();
    }

    //source

    @GetMapping("/source/tenant/{tenantId}")
    public ResponseEntity<List<SourceDto>> getSourceByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(sourceService.getByTenant(tenantId));
    }

    // list active theo tenant (cho crawler hoặc FE)
    @GetMapping("/source/tenant/{tenantId}/active")
    public ResponseEntity<List<SourceDto>> getSourceActiveByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(sourceService.getActiveByTenant(tenantId));
    }

    // cho crawler chung
    @GetMapping("/source/active")
    public ResponseEntity<List<SourceDto>> getAllActive() {
        return ResponseEntity.ok(sourceService.getAllActive());
    }

    @GetMapping("/source/{id}")
    public ResponseEntity<SourceDto> getSourceById(@PathVariable Long id) {
        return ResponseEntity.ok(sourceService.getById(id));
    }

    @PostMapping("/sources")
    public ResponseEntity<SourceDto> createSource(@RequestBody SourceCreateRequest req) {
        return ResponseEntity.ok(sourceService.create(req));
    }

    @PutMapping("/source/{id}")
    public ResponseEntity<SourceDto> updateSource(@PathVariable Long id,
                                            @RequestBody SourceUpdateRequest req) {
        return ResponseEntity.ok(sourceService.update(id, req));
    }

    @DeleteMapping("/source/{id}")
    public ResponseEntity<Void> deleteSource(@PathVariable Long id) {
        sourceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    //user
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    // lấy user theo tenant
    @GetMapping("/users/tenant/{tenantId}")
    public ResponseEntity<List<UserDto>> getUserByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(userService.getByTenant(tenantId));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreateRequest req) {
        return ResponseEntity.ok(userService.create(req));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                          @RequestBody UserUpdateRequest req) {
        return ResponseEntity.ok(userService.update(id, req));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    //tenant

    @GetMapping("/tenants")
    public ResponseEntity<List<TenantDto>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAll());
    }

    @GetMapping("/tenants/{id}")
    public ResponseEntity<TenantDto> getTenantById(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getById(id));
    }

    @PostMapping("/tenants")
    public ResponseEntity<TenantDto> createTenant(@RequestBody TenantCreateRequest req) {
        return ResponseEntity.ok(tenantService.create(req));
    }

    @PutMapping("/tenants/{id}")
    public ResponseEntity<TenantDto> update(@PathVariable Long id,
                                            @RequestBody TenantUpdateRequest req) {
        return ResponseEntity.ok(tenantService.update(id, req));
    }

    @DeleteMapping("/tenants/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
