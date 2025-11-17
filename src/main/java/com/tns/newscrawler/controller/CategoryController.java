package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.Category.CategoryCreateRequest;
import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Category.CategoryUpdateRequest;
import com.tns.newscrawler.service.Category.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // list cho 1 tenant
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<CategoryDto>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(categoryService.getByTenant(tenantId));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<List<CategoryDto>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
    }

    // list active cho FE user
    @GetMapping("/tenant/{tenantId}/active")
    public ResponseEntity<List<CategoryDto>> getActiveByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(categoryService.getActiveByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody CategoryCreateRequest req) {
        return ResponseEntity.ok(categoryService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(@PathVariable Long id,
                                              @RequestBody CategoryUpdateRequest req) {
        return ResponseEntity.ok(categoryService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
