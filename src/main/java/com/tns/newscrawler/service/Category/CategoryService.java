package com.tns.newscrawler.service.Category;

import com.tns.newscrawler.dto.Category.CategoryCreateRequest;
import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Category.CategoryUpdateRequest;

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
}
