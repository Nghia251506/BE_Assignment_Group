package com.tns.newscrawler.service.Category;

import com.tns.newscrawler.dto.Category.CategoryCreateRequest;
import com.tns.newscrawler.dto.Category.CategoryDto;
import com.tns.newscrawler.dto.Category.CategoryUpdateRequest;
import com.tns.newscrawler.entity.Category;
import com.tns.newscrawler.mapper.Category.CategoryMapper;
import com.tns.newscrawler.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll()
                .stream().map(CategoryMapper::toDto).toList();
    }

    @Override
    public List<CategoryDto> getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public List<Category> getAllParentCategories() {
        return categoryRepository.findByParentIdIsNull();
    }
    @Override
    public List<Category> getCategoriesByParentId(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    @Override
    public Long CountCat() {
        return categoryRepository.CountCat();
    }

    @Override
    public List<CategoryDto> getActiveByTenant() {
        return categoryRepository.findByIsActiveTrue()
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
        // 1. create
        Category c = Category.builder()
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
        // Tuỳ: xóa hẳn hoặc chuyển inactive
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        c.setIsActive(false);
        // Nếu muốn xoá hẳn:
        // categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryDto> getPublicCategories() {
        return categoryRepository
                .findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository
                .findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryMapper.toDto(category);
    }

    @Override
    public Page<CategoryDto> searchAdmin(String keyword, Boolean active, Pageable pageable) {
        // Anh tự define thêm method trong repo nếu cần search nâng cao.
        // Tạm thời dùng findAll + filter đơn giản hoặc viết query riêng.
        throw new UnsupportedOperationException("Implement me");
    }

    @Override
    public CategoryDto createCategory(CategoryDto dto) {
        Category entity = CategoryMapper.toEntity(dto);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE);
        Category saved = categoryRepository.save(entity);
        return CategoryMapper.toDto(saved);
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        CategoryMapper.updateEntity(dto, entity);
        Category saved = categoryRepository.save(entity);
        return CategoryMapper.toDto(saved);
    }

    @Override
    public void toggleActive(Long id, boolean isActive) {
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        entity.setIsActive(isActive);
        categoryRepository.save(entity);
    }
}
