package com.tns.newscrawler.service.Tenant;

import com.tns.newscrawler.dto.Tenant.TenantCreateRequest;
import com.tns.newscrawler.dto.Tenant.TenantDto;
import com.tns.newscrawler.dto.Tenant.TenantUpdateRequest;
import com.tns.newscrawler.entity.Tenant;
import com.tns.newscrawler.mapper.Tenant.TenantMapper;
import com.tns.newscrawler.repository.TenantRepository;
import com.tns.newscrawler.service.Tenant.TenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    public TenantServiceImpl(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public List<TenantDto> getAll() {
        return tenantRepository.findAll()
                .stream()
                .map(TenantMapper::toDto)
                .toList();
    }

    @Override
    public TenantDto getById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return TenantMapper.toDto(tenant);
    }

    @Override
    public TenantDto create(TenantCreateRequest req) {
        if (tenantRepository.existsByCode(req.getCode())) {
            throw new RuntimeException("Tenant code already exists");
        }
        Tenant tenant = Tenant.builder()
                .code(req.getCode())
                .name(req.getName())
                .contactEmail(req.getContactEmail())
                .contactPhone(req.getContactPhone())
                .maxUsers(req.getMaxUsers() != null ? req.getMaxUsers() : 5)
                .build();
        tenantRepository.save(tenant);
        return TenantMapper.toDto(tenant);
    }

    @Override
    public TenantDto update(Long id, TenantUpdateRequest req) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        if (req.getName() != null) tenant.setName(req.getName());
        tenant.setContactEmail(req.getContactEmail());
        tenant.setContactPhone(req.getContactPhone());
        if (req.getMaxUsers() != null) tenant.setMaxUsers(req.getMaxUsers());
        if (req.getStatus() != null) {
            tenant.setStatus(Tenant.TenantStatus.valueOf(req.getStatus()));
        }
        // @PreUpdate sẽ tự set updatedAt
        return TenantMapper.toDto(tenant);
    }

    @Override
    public void delete(Long id) {
        // ở đây có thể chuyển sang SUSPENDED thay vì xóa thật
        tenantRepository.deleteById(id);
    }
}
