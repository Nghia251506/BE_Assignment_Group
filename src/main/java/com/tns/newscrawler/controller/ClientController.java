package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Post.PostDto;
import com.tns.newscrawler.service.Category.CategoryService;
import com.tns.newscrawler.service.Post.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class ClientController {
    private final CategoryService categoryService;
    private final PostService postService;

    public ClientController(CategoryService categoryService, PostService postService) {
        this.categoryService = categoryService;
        this.postService = postService;
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(@RequestHeader("X-Tenant-Id") Long tenantId) {
        return categoryService.getPublicCategories(tenantId);
    }

    @GetMapping("/categories/{slug}")
    public CategoryDto getCategoryDetail(@RequestHeader("X-Tenant-Id") Long tenantId,
                                         @PathVariable String slug) {
        return categoryService.getCategoryBySlug(tenantId, slug);
    }

//    @GetMapping("/{slug}/posts")
//    public Page<PostSummaryDto> getCategoryPosts(@RequestHeader("X-Tenant-Id") Long tenantId,
//                                                 @PathVariable String slug,
//                                                 Pageable pageable) {
//        return categoryService.getPostsByCategorySlug(tenantId, slug, pageable);
//    }

    @GetMapping("/posts")
    public Page<PostDto> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postService.getAllPosts(pageable);
    }
    @GetMapping("/posts/{slug}")
    public ResponseEntity<PostDto> getById(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getBySlug(slug));
    }
}
