package com.tns.newscrawler.service.Tag;

import com.tns.newscrawler.dto.Tag.*;
import com.tns.newscrawler.entity.Tag;
import com.tns.newscrawler.mapper.Tag.TagMapper;
import com.tns.newscrawler.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepo;

    public TagServiceImpl(TagRepository tagRepo) {
        this.tagRepo = tagRepo;
    }

    @Override
    public List<TagDto> listTags() {
        return tagRepo.findAll().stream().map(TagMapper::toDto).toList();
    }

    @Override
    public TagDto getById(Long id) {
        var tag = tagRepo.findById(id).orElseThrow(() -> new RuntimeException("Tag not found"));
        return TagMapper.toDto(tag);
    }

    @Override
    public TagDto create(TagCreateRequest req) {
        if (tagRepo.existsByNameIgnoreCase(req.getName())) {
            throw new RuntimeException("Tag name already exists");
        }
        var tag = Tag.builder()
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
    public TagDto upsertByName(String name) {
        var ex = tagRepo.findByNameIgnoreCase(name).orElse(null);
        if (ex != null) return TagMapper.toDto(ex);
        var tag = Tag.builder()
                .name(name)
                .slug(name.trim().toLowerCase().replaceAll("\\s+","-"))
                .build();
        tagRepo.save(tag);
        return TagMapper.toDto(tag);
    }

    @Override
    public List<TagDto> suggestTags(String keyword, int limit) {
        return tagRepo.findByNameContainingIgnoreCase(keyword).stream().map(TagMapper::toDto).limit(limit).toList();
    }

    @Override
    public TagDto getBySlug(String slug) {
        Tag tag = tagRepo.findBySlug(slug).orElseThrow(() -> new RuntimeException("Tag not found"));
        return TagMapper.toDto(tag);
    }

    @Override
    public Page<TagDto> searchAdmin(String keyword, Pageable pageable) {
        return (Page<TagDto>) tagRepo.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(TagMapper::toDto)
                .toList();
    }

    @Override
    public TagDto createTag(TagDto dto) {
        Tag tag = TagMapper.toEntity(dto);
        tagRepo.save(tag);
        return TagMapper.toDto(tag);
    }

    @Override
    public TagDto updateTag(Long id, TagDto dto) {
        Tag tag = tagRepo.findById(id).orElseThrow(() -> new RuntimeException("Tag not found"));
        TagMapper.updateEntity(dto, tag);
        tagRepo.save(tag);
        return TagMapper.toDto(tag);
    }

    @Override
    public void deleteTag(Long id) {
        tagRepo.deleteById(id);
    }
}
