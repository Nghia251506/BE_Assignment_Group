package com.tns.newscrawler.service.Category;

import com.tns.newscrawler.dto.Category.CategoryCreateRequest;
import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Category.CategoryUpdateRequest;
import com.tns.newscrawler.entity.Category;
import com.tns.newscrawler.entity.Tenant;
import com.tns.newscrawler.mapper.Category.CategoryMapper;
import com.tns.newscrawler.mapper.Post.PostMapper;
import com.tns.newscrawler.repository.CategoryRepository;
import com.tns.newscrawler.repository.TenantRepository;
import com.tns.newscrawler.service.Category.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    public List<CategoryDto> getPublicCategories(Long tenantId) {
        return categoryRepository
                .findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId)
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryBySlug(Long tenantId, String slug) {
        Category category = categoryRepository
                .findByTenantIdAndSlugAndIsActiveTrue(tenantId, slug)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryMapper.toDto(category);
    }

//    @Override
//    public Page<PostSummaryDto> getPostsByCategorySlug(Long tenantId, String slug, Pageable pageable) {
//        Category category = categoryRepository
//                .findByTenantIdAndSlugAndIsActiveTrue(tenantId, slug)
//                .orElseThrow(() -> new RuntimeException("Category not found"));
//
//        Page<Post> page = postRepository
//                .findByTenantIdAndCategoryIdAndStatusAndDeleteStatusOrderByPublishedAtDesc(
//                        tenantId, category.getId(), "PUBLISHED", "NORMAL", pageable);
//
//        return page.map(PostMapper::toSummaryDto);
//    }

    @Override
    public Page<CategoryDto> searchAdmin(Long tenantId, String keyword, Boolean active, Pageable pageable) {
        // Anh tự define thêm method trong repo nếu cần search nâng cao.
        // Tạm thời dùng findAll + filter đơn giản hoặc viết query riêng.
        throw new UnsupportedOperationException("Implement me");
    }

    @Override
    public CategoryDto createCategory(Long tenantId, CategoryDto dto) {
        Category entity = CategoryMapper.toEntity(dto);
        entity.setTenantId(tenantId);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE);
        Category saved = categoryRepository.save(entity);
        return CategoryMapper.toDto(saved);
    }

    @Override
    public CategoryDto updateCategory(Long tenantId, Long id, CategoryDto dto) {
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!tenantId.equals(entity.getTenantId())) {
            throw new RuntimeException("Access denied");
        }

        CategoryMapper.updateEntity(dto, entity);
        Category saved = categoryRepository.save(entity);
        return CategoryMapper.toDto(saved);
    }

    @Override
    public void toggleActive(Long tenantId, Long id, boolean isActive) {
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (!tenantId.equals(entity.getTenant())) {
            throw new RuntimeException("Access denied");
        }
        entity.setIsActive(isActive);
        categoryRepository.save(entity);
    }
}
