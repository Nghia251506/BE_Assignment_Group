package com.tns.newscrawler.service.Tag;

import com.tns.newscrawler.dto.Tag.*;
import com.tns.newscrawler.entity.Tag;
import com.tns.newscrawler.entity.Tenant;
import com.tns.newscrawler.mapper.Tag.TagMapper;
import com.tns.newscrawler.repository.TagRepository;
import com.tns.newscrawler.repository.TenantRepository;
import com.tns.newscrawler.service.Tag.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepo;
    private final TenantRepository tenantRepo;

    public TagServiceImpl(TagRepository tagRepo, TenantRepository tenantRepo) {
        this.tagRepo = tagRepo;
        this.tenantRepo = tenantRepo;
    }

    @Override
    public List<TagDto> listByTenant(Long tenantId) {
        return tagRepo.findByTenant_Id(tenantId).stream().map(TagMapper::toDto).toList();
    }

    @Override
    public TagDto getById(Long id) {
        var tag = tagRepo.findById(id).orElseThrow(() -> new RuntimeException("Tag not found"));
        return TagMapper.toDto(tag);
    }

    @Override
    public TagDto create(TagCreateRequest req) {
        var tenant = tenantRepo.findById(req.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        if (tagRepo.existsByTenant_IdAndNameIgnoreCase(tenant.getId(), req.getName())) {
            throw new RuntimeException("Tag name already exists in this tenant");
        }
        var tag = Tag.builder()
                .tenant(tenant)
                .name(req.getName())
                .slug(req.getSlug()!=null ? req.getSlug() : req.getName().trim().toLowerCase().replaceAll("\\s+","-"))
                .build();
        tagRepo.save(tag);
        return TagMapper.toDto(tag);
    }

    @Override
    public TagDto update(Long id, TagUpdateRequest req) {
        var tag = tagRepo.findById(id).orElseThrow(() -> new RuntimeException("Tag not found"));
        if (req.getName()!=null) tag.setName(req.getName());
        if (req.getSlug()!=null) tag.setSlug(req.getSlug());
        return TagMapper.toDto(tag);
    }

    @Override
    public void delete(Long id) {
        tagRepo.deleteById(id);
    }

    @Override
    public TagDto upsertByName(Long tenantId, String name) {
        var ex = tagRepo.findByTenant_IdAndNameIgnoreCase(tenantId, name).orElse(null);
        if (ex != null) return TagMapper.toDto(ex);
        var tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        var tag = Tag.builder()
                .tenant(tenant)
                .name(name)
                .slug(name.trim().toLowerCase().replaceAll("\\s+","-"))
                .build();
        tagRepo.save(tag);
        return TagMapper.toDto(tag);
    }
}
