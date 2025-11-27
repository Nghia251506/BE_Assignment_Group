package com.tns.newscrawler.controller;

import com.google.analytics.data.v1beta.*;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.tns.newscrawler.config.Ga4Config;
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
import com.tns.newscrawler.entity.Category;
import com.tns.newscrawler.entity.Post;
import com.tns.newscrawler.entity.Setting;
import com.tns.newscrawler.mapper.Post.PostMapper;
import com.tns.newscrawler.repository.CategoryRepository;
import com.tns.newscrawler.repository.PostRepository;
import com.tns.newscrawler.repository.SourceRepository;
import com.tns.newscrawler.service.Category.CategoryService;
import com.tns.newscrawler.service.Crawler.ContentCrawlerService;
import com.tns.newscrawler.service.Crawler.LinkCrawlerService;
import com.tns.newscrawler.service.Post.PostService;
import com.tns.newscrawler.service.Setting.SettingService;
import com.tns.newscrawler.service.Source.SourceService;
import com.tns.newscrawler.service.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import com.google.analytics.data.v1beta.BetaAnalyticsDataSettings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@Secured("ADMIN")
@RequestMapping("/api/admin")
public class AdminController {
    private final CategoryService categoryService;
    private final LinkCrawlerService linkCrawlerService;
    private final ContentCrawlerService contentCrawlerService;
    private final PostService postService;
    private final SourceService sourceService;
    private final UserService userService;
    private final SettingService SettingService;
    private final PostRepository postRepo;
    private final SourceRepository sourceRepo;
    private final CategoryRepository catRepo;
    private static final String PROPERTY_ID = "514447198";


    public AdminController(CategoryService categoryService,
                           LinkCrawlerService linkCrawlerService,
                           ContentCrawlerService contentCrawlerService,
                           PostService postService, SourceService sourceService,
                           UserService userService, SettingService settingService, PostRepository postRepo, SourceRepository sourceRepo, CategoryRepository catyRepo) {
        this.categoryService = categoryService;
        this.linkCrawlerService = linkCrawlerService;
        this.contentCrawlerService = contentCrawlerService;
        this.postService = postService;
        this.sourceService = sourceService;
        this.userService = userService;

        SettingService = settingService;
        this.postRepo = postRepo;
        this.sourceRepo = sourceRepo;
        this.catRepo = catyRepo;
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

    @GetMapping("/categories/parent")
    public ResponseEntity<List<Category>> getAllParentCategories() {
        List<Category> categories = categoryService.getAllParentCategories();
        return ResponseEntity.ok(categories);
    }

    // API lấy danh mục con của một danh mục cha
    @GetMapping("/categories/{parentId}/children")
    public ResponseEntity<List<Category>> getCategoriesByParentId(@PathVariable Long parentId) {
        List<Category> categories = categoryService.getCategoriesByParentId(parentId);
        return ResponseEntity.ok(categories);
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

    @PostMapping("/posts/{id}/view")
    public ResponseEntity<Void> increaseView(@PathVariable Long id) {
        postService.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(@RequestBody PostCreateRequest req) {
        PostDto postDto = postService.create(req);
        return ResponseEntity.ok(postDto);
    }

    @PostMapping("/posts/{id}/generate")
    public ResponseEntity<Post> generatePost(@PathVariable Long id) {
        Post postDto = postService.generatePost(id);
        return ResponseEntity.ok(postDto);
    }

    @PostMapping("/posts/bulk-action")
    public ResponseEntity<?> bulkActionPost(@RequestBody PostBulkActionRequest req) {
        if ("publish".equals(req.getAction())) {
            postService.bulkPublish(req.getPostIds());
        } else if ("generate".equals(req.getAction())) {
            postService.bulkGenerateAndPublish(req.getPostIds()); // hoặc tách riêng
        }
        return ResponseEntity.ok("Thao tác thành công với " + req.getPostIds().size() + " bài viết");
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

    @GetMapping("/today-stats")
    public ResponseEntity<Map<String, Long>> getTodayStats() {
        try (BetaAnalyticsDataClient client = BetaAnalyticsDataClient.create(betaSettings())) { // ← DÙNG THUẦN TÚY

            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty("properties/" + PROPERTY_ID)
                    .addDateRanges(DateRange.newBuilder().setStartDate("today").setEndDate("today").build())
                    .addMetrics(Metric.newBuilder().setName("screenPageViews").build())
                    .addMetrics(Metric.newBuilder().setName("activeUsers").build())
                    .build();

            RunReportResponse response = client.runReport(request);

            long todayViews = response.getRowsCount() > 0
                    ? Long.parseLong(response.getRows(0).getMetricValues(0).getValue()) : 0L;
            long todayUsers = response.getRowsCount() > 0
                    ? Long.parseLong(response.getRows(0).getMetricValues(1).getValue()) : 0L;

            return ResponseEntity.ok(Map.of("todayViews", todayViews, "todayUsers", todayUsers));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("todayViews", 0L, "todayUsers", 0L));
        }
    }

    private BetaAnalyticsDataSettings betaSettings() {
        return null;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", postRepo.CountPosts());
        stats.put("totalCategories", catRepo.CountCat());
        stats.put("totalSources", sourceRepo.CountSources());
        stats.put("todayUsers", 1247); // tạm hardcode hoặc lấy từ GA4
        stats.put("totalUsers7d", 48392);
        return stats;
    }

    @GetMapping("/posts/recent")
    public ResponseEntity<List<PostDto>> getRecentPosts() {
        List<Post> posts = postRepo.getTop5Recent();

        List<PostDto> dtos = posts.stream()
                .map(PostMapper::toDto)  // hoặc new PostRecentDto(post)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/categories/{slug}/posts-count")
    public int getArticleCountByCategorySlug(@PathVariable String slug) {
        return postService.getArticleCountByCategorySlug(slug);
    }

    @GetMapping("/sources/{id}/posts-count")
    public int getArticleCountBySourceId(@PathVariable Long id) {
        return postService.getArticleCountBySourceId(id);
    }

    @GetMapping("/posts/filter")
    public ResponseEntity<List<PostDto>> getPostsByCategory(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long parentId) {

        // Kiểm tra nếu không có categoryId và parentId thì trả về lỗi
        if (categoryId == null && parentId == null) {
            return ResponseEntity.badRequest().body(null);
        }

        List<PostDto> posts = postService.getPostsByCategory(categoryId, parentId);
        return ResponseEntity.ok(posts);
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
    public ResponseEntity<Setting> updateSettings(Long Id, @RequestBody Setting setting) {
        Setting updatedSetting = SettingService.updateSettings(Id, setting);
        return ResponseEntity.ok(updatedSetting);
    }

    @PostMapping("/settings")
    public ResponseEntity<Setting> createSetting(@RequestBody Setting setting) {
        Setting createdSetting = SettingService.createSetting(setting);
        return ResponseEntity.ok(createdSetting);
    }
    @Autowired
    private GoogleCredentials googleCredentials;
    //Ga4
    @GetMapping("/weekly-users")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyUsers() throws Exception {

        BetaAnalyticsDataSettings settings = BetaAnalyticsDataSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
                .build();

        try (BetaAnalyticsDataClient client = BetaAnalyticsDataClient.create(settings)) {
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty("properties/" + "514447198")  // hoặc ga4Config.getPropertyId()
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate("7daysAgo")
                            .setEndDate("today")
                            .build())
                    // SAI TRƯỚC ĐÂY → ĐÚNG BÂY GIỜ:
                    .addDimensions(Dimension.newBuilder().setName("date").build())           // chỉ 1 lần
                    .addMetrics(Metric.newBuilder().setName("activeUsers").build())
                    .build();

            RunReportResponse response = client.runReport(request);

            List<Map<String, Object>> result = new ArrayList<>();
            String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};

            for (Row row : response.getRowsList()) {
                String dateStr = row.getDimensionValues(0).getValue(); // "20251120"
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
                int dayIndex = date.getDayOfWeek().getValue() % 7; // 1=Mon → 0, 7=Sun → 6
                String dayName = dayNames[dayIndex == 0 ? 6 : dayIndex - 1];

                // SAI TRƯỚC ĐÂY → ĐÚNG BÂY GIỜ:
                String valueStr = row.getMetricValues(0).getValue();   // trả về String
                long users = valueStr == null || valueStr.isEmpty() ? 0 : Long.parseLong(valueStr);

                result.add(Map.of("name", dayName, "users", users));
            }

            // Sắp xếp từ CN → T7
            result.sort(Comparator.comparingInt(m -> Arrays.asList(dayNames).indexOf(m.get("name"))));

            return ResponseEntity.ok(result);
        }
    }
}


