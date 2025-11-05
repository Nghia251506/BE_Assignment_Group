package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.Source.SourceCreateRequest;
import com.tns.newscrawler.dto.Source.SourceDto;
import com.tns.newscrawler.dto.Source.SourceUpdateRequest;
import com.tns.newscrawler.service.Source.SourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sources")
public class SourceController {

    private final SourceService sourceService;

    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    // list theo tenant
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<SourceDto>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(sourceService.getByTenant(tenantId));
    }

    // list active theo tenant (cho crawler hoặc FE)
    @GetMapping("/tenant/{tenantId}/active")
    public ResponseEntity<List<SourceDto>> getActiveByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(sourceService.getActiveByTenant(tenantId));
    }

    // cho crawler chung
    @GetMapping("/active")
    public ResponseEntity<List<SourceDto>> getAllActive() {
        return ResponseEntity.ok(sourceService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sourceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SourceDto> create(@RequestBody SourceCreateRequest req) {
        return ResponseEntity.ok(sourceService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SourceDto> update(@PathVariable Long id,
                                            @RequestBody SourceUpdateRequest req) {
        return ResponseEntity.ok(sourceService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
