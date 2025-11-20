package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.Tag.*;
import com.tns.newscrawler.service.Tag.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagDto>> list() {
        return ResponseEntity.ok(tagService.listTags());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TagDto> create(@RequestBody TagCreateRequest req) {
        return ResponseEntity.ok(tagService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDto> update(@PathVariable Long id, @RequestBody TagUpdateRequest req) {
        return ResponseEntity.ok(tagService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // upsert theo name (tiện khi admin nhập tag mới tại form post)
    @PostMapping("/upsert")
    public ResponseEntity<TagDto> upsert(@RequestParam String name) {
        return ResponseEntity.ok(tagService.upsertByName(name));
    }
}
