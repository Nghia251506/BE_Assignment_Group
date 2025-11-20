package com.tns.newscrawler.service.Category;

import com.tns.newscrawler.dto.Category.CategoryCreateRequest;
import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Category.CategoryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    List<CategoryDto> getByTenant(Long tenantId);
    List<CategoryDto> getBySlug(String slug);
    List<CategoryDto> getCategories();

    List<CategoryDto> getActiveByTenant(Long tenantId);

    CategoryDto getById(Long id);

    CategoryDto create(CategoryCreateRequest req);

    CategoryDto update(Long id, CategoryUpdateRequest req);

    void delete(Long id);
    List<CategoryDto> getPublicCategories(Long tenantId);

    CategoryDto getCategoryBySlug(Long tenantId, String slug);

//    Page<PostSummaryDto> getPostsByCategorySlug(Long tenantId, String slug, Pageable pageable);

    // Admin
    Page<CategoryDto> searchAdmin(Long tenantId, String keyword, Boolean active, Pageable pageable);

    CategoryDto createCategory(Long tenantId, CategoryDto dto);

    CategoryDto updateCategory(Long tenantId, Long id, CategoryDto dto);

    void toggleActive(Long tenantId, Long id, boolean isActive);
}
