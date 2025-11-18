package com.tns.newscrawler.service.Category;

import com.tns.newscrawler.dto.Category.CategoryCreateRequest;
import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Category.CategoryUpdateRequest;
import com.tns.newscrawler.entity.Category;
import com.tns.newscrawler.entity.Tenant;
import com.tns.newscrawler.mapper.Category.CategoryMapper;
import com.tns.newscrawler.repository.CategoryRepository;
import com.tns.newscrawler.repository.TenantRepository;
import com.tns.newscrawler.service.Category.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TenantRepository tenantRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               TenantRepository tenantRepository) {
        this.categoryRepository = categoryRepository;
        this.tenantRepository = tenantRepository;
    }
    @Override
    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll()
                .stream().map(CategoryMapper::toDto).toList();
    }
    @Override
    public List<CategoryDto> getByTenant(Long tenantId) {
        return categoryRepository.findByTenant_Id(tenantId)
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public List<CategoryDto> getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public List<CategoryDto> getActiveByTenant(Long tenantId) {
        return categoryRepository.findByTenant_IdAndIsActiveTrue(tenantId)
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getById(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryMapper.toDto(c);
    }


    @Override
    public CategoryDto create(CategoryCreateRequest req) {
        // 1. check tenant
        Tenant tenant = tenantRepository.findById(req.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // 2. check code unique trong tenant
        if (categoryRepository.existsByTenant_IdAndCode(tenant.getId(), req.getCode())) {
            throw new RuntimeException("Category code already exists in this tenant");
        }

        // 3. create
        Category c = Category.builder()
                .tenant(tenant)
                .code(req.getCode())
                .name(req.getName())
                .description(req.getDescription())
                .isActive(true)
                .build();

        categoryRepository.save(c);
        return CategoryMapper.toDto(c);
    }

    @Override
    public CategoryDto update(Long id, CategoryUpdateRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (req.getName() != null) c.setName(req.getName());
        c.setDescription(req.getDescription());
        if (req.getIsActive() != null) c.setIsActive(req.getIsActive());

        return CategoryMapper.toDto(c);
    }

    @Override
    public void delete(Long id) {
        // tuỳ: xóa hẳn hoặc chuyển inactive
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        c.setIsActive(false);
        // nếu muốn xoá hẳn:
        // categoryRepository.deleteById(id);
    }
}
