package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.Tenant.TenantCreateRequest;
import com.tns.newscrawler.dto.Tenant.TenantDto;
import com.tns.newscrawler.dto.Tenant.TenantUpdateRequest;
import com.tns.newscrawler.service.Tenant.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public ResponseEntity<List<TenantDto>> getAll() {
        return ResponseEntity.ok(tenantService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TenantDto> create(@RequestBody TenantCreateRequest req) {
        return ResponseEntity.ok(tenantService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantDto> update(@PathVariable Long id,
                                            @RequestBody TenantUpdateRequest req) {
        return ResponseEntity.ok(tenantService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
