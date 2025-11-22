package com.tns.newscrawler.service.Category;

import com.tns.newscrawler.dto.Category.CategoryCreateRequest;
import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Category.CategoryUpdateRequest;
import com.tns.newscrawler.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    List<CategoryDto> getCategories();
    List<CategoryDto> getBySlug(String slug);
    List<CategoryDto> getActiveByTenant();  // Bỏ tenantId

    CategoryDto getById(Long id);

    CategoryDto create(CategoryCreateRequest req);

    CategoryDto update(Long id, CategoryUpdateRequest req);

    void delete(Long id);
    List<CategoryDto> getPublicCategories();

    CategoryDto getCategoryBySlug(String slug);

    // Admin
    Page<CategoryDto> searchAdmin(String keyword, Boolean active, Pageable pageable);

    CategoryDto createCategory(CategoryDto dto);

    CategoryDto updateCategory(Long id, CategoryDto dto);

    void toggleActive(Long id, boolean isActive);

    List<Category> getAllParentCategories();
    List<Category> getCategoriesByParentId(Long parentId);
}
