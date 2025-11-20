package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByTenant_Id(Long tenantId);
    Optional<Tag> findByTenant_IdAndNameIgnoreCase(Long tenantId, String name);
    boolean existsByTenant_IdAndNameIgnoreCase(Long tenantId, String name);
    List<Tag> findByTenantIdAndNameContainingIgnoreCase(Long tenantId, String keyword);

    Optional<Tag> findByTenantIdAndSlug(Long tenantId, String slug);
}
