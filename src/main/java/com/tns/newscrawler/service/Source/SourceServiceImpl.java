package com.tns.newscrawler.service.Source;

import com.tns.newscrawler.dto.Source.SourceCreateRequest;
import com.tns.newscrawler.dto.Source.SourceDto;
import com.tns.newscrawler.dto.Source.SourceUpdateRequest;
import com.tns.newscrawler.entity.Category;
import com.tns.newscrawler.entity.Source;
import com.tns.newscrawler.entity.Tenant;
import com.tns.newscrawler.mapper.Source.SourceMapper;
import com.tns.newscrawler.repository.CategoryRepository;
import com.tns.newscrawler.repository.SourceRepository;
import com.tns.newscrawler.repository.TenantRepository;
import com.tns.newscrawler.service.Source.SourceService;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class SourceServiceImpl implements SourceService {

    private final SourceRepository sourceRepository;
    private final TenantRepository tenantRepository;
    private final CategoryRepository categoryRepository;

    public SourceServiceImpl(SourceRepository sourceRepository,
                             TenantRepository tenantRepository,
                             CategoryRepository categoryRepository) {
        this.sourceRepository = sourceRepository;
        this.tenantRepository = tenantRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<SourceDto> getByTenant(Long tenantId) {
        return sourceRepository.findByTenant_Id(tenantId)
                .stream()
                .map(SourceMapper::toDto)
                .toList();
    }

    @Override
    public List<SourceDto> getActiveByTenant(Long tenantId) {
        return sourceRepository.findByTenant_IdAndIsActiveTrue(tenantId)
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
        Tenant tenant = tenantRepository.findById(req.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // nếu muốn tránh trùng list_url trong cùng tenant
        if (sourceRepository.existsByTenant_IdAndListUrl(tenant.getId(), req.getListUrl())) {
            throw new RuntimeException("This list url already exists in this tenant");
        }

        Source s = Source.builder()
                .tenant(tenant)
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
        // có thể đổi isActive = false thay vì xóa
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

    //bot crawler url
    @Override
    public String runbot(){
        List<Source> sourceList = sourceRepository.findAll();
        try{
            for(Source s : sourceList){
                Document document = Jsoup.connect(s.getBaseUrl()).get();
                Element elements = document.select(s.getListUrl()).first();
                HashSet<String> links = new HashSet<>();
                for(Element element : elements){

                }
            }
        }catch(Exception e){

        }
        return runbot();
    }
}
