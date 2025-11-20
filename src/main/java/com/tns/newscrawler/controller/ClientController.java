package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Post.PostDetailDto;
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
    public List<CategoryDto> getCategories() {
        return categoryService.getCategories();  // Đã bỏ tenantId, gọi getCategories mà không cần tenantId
    }

    @GetMapping("/categories/{slug}")
    public CategoryDto getCategoryDetail(@PathVariable String slug) {
        return categoryService.getCategoryBySlug(slug);  // Đã bỏ tenantId, gọi getCategoryBySlug mà không cần tenantId
    }

    @GetMapping("/posts")
    public Page<PostDto> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postService.getLatestPosts(pageable);  // Sử dụng phương thức getLatestPosts thay cho getAllPosts
    }

    @GetMapping("/posts/{slug}")
    public ResponseEntity<PostDetailDto> getPostBySlug(@PathVariable String slug) {
        PostDetailDto postDto = postService.getPostBySlug(slug);  // Gọi phương thức getPostBySlug mà không cần tenantId
        return ResponseEntity.ok(postDto);
    }
}

