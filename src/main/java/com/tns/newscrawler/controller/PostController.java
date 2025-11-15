package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.Post.*;
import com.tns.newscrawler.service.Post.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostController {

    private final PostService postService;
    public PostController(PostService postService) { this.postService = postService; }

    @GetMapping("/api/admin/posts")
    public Page<PostDto> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postService.getAllPosts(pageable);
    }

    // ==== ADMIN ====
    @GetMapping("/api/admin/posts/{id}")
    public ResponseEntity<PostDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getById(id));
    }

    @PostMapping("/api/admin/posts")
    public ResponseEntity<PostDto> create(@RequestBody PostCreateRequest req) {
        return ResponseEntity.ok(postService.create(req));
    }

    @PutMapping("/api/admin/posts/{id}")
    public ResponseEntity<PostDto> update(@PathVariable Long id, @RequestBody PostUpdateRequest req) {
        return ResponseEntity.ok(postService.update(id, req));
    }

    @PutMapping("/api/admin/posts/{id}/publish")
    public ResponseEntity<PostDto> publish(@PathVariable Long id) {
        return ResponseEntity.ok(postService.publish(id));
    }

    @DeleteMapping("/api/admin/posts/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id,
                                           @RequestParam(required=false) Long userId) {
        postService.softDelete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/admin/posts/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        postService.restore(id);
        return ResponseEntity.noContent().build();
    }

    // ==== CLIENT (public) ====
    @PostMapping("/api/posts/search")
    public ResponseEntity<Page<PostDto>> search(@RequestBody PostSearchRequest req) {
        return ResponseEntity.ok(postService.search(req));
    }

    // ==== CRAWLER support ====
    @GetMapping("/api/crawler/posts/exists")
    public ResponseEntity<Boolean> exists(@RequestParam String originUrl) {
        return ResponseEntity.ok(postService.existsByOrigin(originUrl));
    }

    // idempotent upsert theo originUrl
    @PostMapping("/api/crawler/posts/upsert")
    public ResponseEntity<PostDto> upsert(@RequestBody PostCreateRequest req) {
        return ResponseEntity.ok(postService.upsertByOrigin(req));
    }
}
