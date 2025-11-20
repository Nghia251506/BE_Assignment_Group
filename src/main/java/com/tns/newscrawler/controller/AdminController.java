package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.Category.CategoryCreateRequest;
import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Category.CategoryUpdateRequest;
import com.tns.newscrawler.dto.Post.*;
import com.tns.newscrawler.dto.Source.SourceCreateRequest;
import com.tns.newscrawler.dto.Source.SourceDto;
import com.tns.newscrawler.dto.Source.SourceUpdateRequest;
import com.tns.newscrawler.dto.User.UserCreateRequest;
import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.User.UserUpdateRequest;
import com.tns.newscrawler.entity.Setting;
import com.tns.newscrawler.repository.SettingRepository;
import com.tns.newscrawler.service.Category.CategoryService;
import com.tns.newscrawler.service.Crawler.ContentCrawlerService;
import com.tns.newscrawler.service.Crawler.LinkCrawlerService;
import com.tns.newscrawler.service.Post.PostService;
import com.tns.newscrawler.service.Setting.SettingService;
import com.tns.newscrawler.service.Source.SourceService;
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
    private final SettingService SettingService;

    public AdminController(CategoryService categoryService,
                           LinkCrawlerService linkCrawlerService,
                           ContentCrawlerService contentCrawlerService,
                           PostService postService, SourceService sourceService,
                           UserService userService, SettingService settingService) {
        this.categoryService = categoryService;
        this.linkCrawlerService = linkCrawlerService;
        this.contentCrawlerService = contentCrawlerService;
        this.postService = postService;
        this.sourceService = sourceService;
        this.userService = userService;

        SettingService = settingService;
    }

    // Category
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @GetMapping("/categories/{slug}")
    public ResponseEntity<List<CategoryDto>> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
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

    // Crawler
    @PostMapping("/crawler/links/all")
    public String crawlAll() {
        int total = linkCrawlerService.crawlAllActiveSources();
        return "Upsert " + total + " links for all active sources";
    }


    @PostMapping("/crawler/content/by-source")
    public String crawlContentBySource(@RequestParam Long sourceId,
                                       @RequestParam(defaultValue = "20") int limit) {
        int ok = contentCrawlerService.crawlPendingBySource(sourceId, limit);
        return "Crawled content for " + ok + " posts of source " + sourceId;
    }

    // Post
    @GetMapping("/posts")
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status, // Thêm điều kiện status nếu cần
            @RequestParam(required = false) Long categoryId, // Thêm categoryId nếu muốn filter theo category
            @RequestParam(required = false) Long sourceId) {  // Thêm sourceId nếu muốn filter theo source

        PostSearchRequest req = new PostSearchRequest();
        req.setPage(page);
        req.setSize(size);
        req.setKeyword(keyword);
        req.setStatus(status);
        req.setCategoryId(categoryId);
        req.setSourceId(sourceId);

        Page<PostDto> posts = postService.search(req);  // Gọi search thay vì getLatestPosts

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{slug}")
    public ResponseEntity<PostDetailDto> getPostBySlug(@PathVariable String slug) {
        PostDetailDto postDto = postService.getPostBySlug(slug);
        return ResponseEntity.ok(postDto);
    }

    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(@RequestBody PostCreateRequest req) {
        PostDto postDto = postService.create(req);
        return ResponseEntity.ok(postDto);
    }
    @PostMapping("/posts/{id}/generate")
    public ResponseEntity<PostDto> generatePost(@PathVariable Long id){
        PostDto postDto = postService.generatePost(id);
        return ResponseEntity.ok(postDto);
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @RequestBody PostUpdateRequest req) {
        PostDto postDto = postService.update(id, req);
        return ResponseEntity.ok(postDto);
    }

    @PutMapping("/posts/{id}/publish")
    public ResponseEntity<PostDto> publishPost(@PathVariable Long id) {
        PostDto postDto = postService.publishPost(id);
        return ResponseEntity.ok(postDto);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> softDeletePost(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        postService.softDeletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories/{slug}/posts-count")
    public int getArticleCountByCategorySlug(@PathVariable String slug) {
        return postService.getArticleCountByCategorySlug(slug);
    }

    // Source
    @GetMapping("/sources")
    public ResponseEntity<List<SourceDto>> getSources() {
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

    @PutMapping("/sources/{id}")
    public ResponseEntity<SourceDto> updateSource(@PathVariable Long id, @RequestBody SourceUpdateRequest req) {
        return ResponseEntity.ok(sourceService.update(id, req));
    }

    @DeleteMapping("/sources/{id}")
    public ResponseEntity<Void> deleteSource(@PathVariable Long id) {
        sourceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // User
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAll());
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
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest req) {
        return ResponseEntity.ok(userService.update(id, req));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    //setting
    // Lấy cài đặt SEO và Auto-Crawler
    @GetMapping("/settings")
    public ResponseEntity<Setting> getSettings() {
        Setting setting = SettingService.getSettings();
        return ResponseEntity.ok(setting);
    }

    // Cập nhật cài đặt SEO và Auto-Crawler
    @PutMapping("/settings")
    public ResponseEntity<Setting> updateSettings(Long Id,@RequestBody Setting setting) {
        Setting updatedSetting = SettingService.updateSettings(Id,setting);
        return ResponseEntity.ok(updatedSetting);
    }
    @PostMapping("/settings")
    public ResponseEntity<Setting> createSetting(@RequestBody Setting setting) {
        Setting createdSetting = SettingService.createSetting(setting);
        return ResponseEntity.ok(createdSetting);
    }
}
