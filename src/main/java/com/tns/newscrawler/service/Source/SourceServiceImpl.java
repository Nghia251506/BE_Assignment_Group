package com.tns.newscrawler.service.Source;

import com.tns.newscrawler.dto.Source.SourceCreateRequest;
import com.tns.newscrawler.dto.Source.SourceDto;
import com.tns.newscrawler.dto.Source.SourceUpdateRequest;
import com.tns.newscrawler.entity.Category;
import com.tns.newscrawler.entity.Source;
import com.tns.newscrawler.mapper.Source.SourceMapper;
import com.tns.newscrawler.repository.CategoryRepository;
import com.tns.newscrawler.repository.SourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SourceServiceImpl implements SourceService {

    private final SourceRepository sourceRepository;
    private final CategoryRepository categoryRepository;

    public SourceServiceImpl(SourceRepository sourceRepository,
                             CategoryRepository categoryRepository) {
        this.sourceRepository = sourceRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<SourceDto> getActiveByTenant() {
        // Lấy tất cả nguồn active
        return sourceRepository.findByIsActiveTrue()
                .stream()
                .map(SourceMapper::toDto)
                .toList();
    }

    @Override
    public SourceDto getById(Long id) {
        Source s = sourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Source not found"));
        return SourceMapper.toDto(s);
    }

    @Override
    public SourceDto create(SourceCreateRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Check nếu list_url đã tồn tại trong DB
        if (sourceRepository.existsByListUrl(req.getListUrl())) {
            throw new RuntimeException("This list URL already exists");
        }

        Source s = Source.builder()
                .category(category)
                .name(req.getName())
                .baseUrl(req.getBaseUrl())
                .listUrl(req.getListUrl())
                .listItemSelector(req.getListItemSelector())
                .linkAttr(req.getLinkAttr() != null ? req.getLinkAttr() : "href")
                .titleSelector(req.getTitleSelector())
                .contentSelector(req.getContentSelector())
                .thumbnailSelector(req.getThumbnailSelector())
                .authorSelector(req.getAuthorSelector())
                .note(req.getNote())
                .isActive(true)
                .build();

        sourceRepository.save(s);
        return SourceMapper.toDto(s);
    }

    @Override
    public SourceDto update(Long id, SourceUpdateRequest req) {
        Source s = sourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Source not found"));

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            s.setCategory(category);
        }

        if (req.getName() != null) s.setName(req.getName());
        if (req.getBaseUrl() != null) s.setBaseUrl(req.getBaseUrl());
        if (req.getListUrl() != null) s.setListUrl(req.getListUrl());
        if (req.getListItemSelector() != null) s.setListItemSelector(req.getListItemSelector());
        if (req.getLinkAttr() != null) s.setLinkAttr(req.getLinkAttr());
        if (req.getTitleSelector() != null) s.setTitleSelector(req.getTitleSelector());
        if (req.getContentSelector() != null) s.setContentSelector(req.getContentSelector());
        if (req.getThumbnailSelector() != null) s.setThumbnailSelector(req.getThumbnailSelector());
        if (req.getAuthorSelector() != null) s.setAuthorSelector(req.getAuthorSelector());
        if (req.getIsActive() != null) s.setIsActive(req.getIsActive());
        if (req.getNote() != null) s.setNote(req.getNote());

        return SourceMapper.toDto(s);
    }

    @Override
    public void delete(Long id) {
        // Thay vì xóa hoàn toàn, chỉ đánh dấu không hoạt động
        Source s = sourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Source not found"));
        s.setIsActive(false);
    }

    @Override
    public List<SourceDto> getAllActive() {
        return sourceRepository.findByIsActiveTrue()
                .stream()
                .map(SourceMapper::toDto)
                .toList();
    }
}
