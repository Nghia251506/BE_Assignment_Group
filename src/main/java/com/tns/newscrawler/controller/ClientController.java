package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Post.PostDetailDto;
import com.tns.newscrawler.dto.Post.PostDto;
import com.tns.newscrawler.service.Category.CategoryService;
import com.tns.newscrawler.service.Post.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.JedisPooled;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class ClientController {
    private final CategoryService categoryService;
    private final PostService postService;
    @Autowired
    private JedisPooled jedis;

    public ClientController(CategoryService categoryService, PostService postService) {
        this.categoryService = categoryService;
        this.postService = postService;
    }

    @PostMapping("/posts/{id}/view")
    public ResponseEntity<Void> increaseView(@PathVariable Long id) {
        postService.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories() {
        return categoryService.getCategories();  // Đã bỏ tenantId, gọi getCategories mà không cần tenantId
    }

    @GetMapping("/categories/{slug}")
    public CategoryDto getCategoryDetail(@PathVariable String slug) {
        return categoryService.getCategoryBySlug(slug);  // Đã bỏ tenantId, gọi getCategoryBySlug mà không cần tenantId
    }

    @GetMapping("/posts")
    public Page<PostDto> getAllPosts(
            @RequestParam(required = false) String categorySlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishedAt,desc") String sort) {  // thêm sort để linh hoạt

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));

        // ĐÂY LÀ DÒNG CỨU CẢ DỰ ÁN
        if (categorySlug != null && !categorySlug.trim().isEmpty()) {
            return postService.getPostsByCategorySlug(categorySlug, pageable);
        }

        return postService.getLatestPosts(pageable);
    }

    @GetMapping("/posts/{slug}")
    public ResponseEntity<PostDetailDto> getPostBySlug(@PathVariable String slug) {
        PostDetailDto postDto = postService.getPostBySlug(slug);  // Gọi phương thức getPostBySlug mà không cần tenantId
        return ResponseEntity.ok(postDto);
    }

    @GetMapping("/test-redis")
    public String testRedis() {
        try {
            jedis.set("railway-test", "OK from " + new Date());
            String value = jedis.get("railway-test");
            jedis.del("railway-test");
            return "Redis OK: " + value + " | Total keys: " + jedis.dbSize();
        } catch (Exception e) {
            return "Redis FAIL: " + e.getMessage() + " | Stack: " + e.getStackTrace()[0];
        }
    }
}

