package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByTenant_Id(Long tenantId);

    List<Category> findByTenant_IdAndIsActiveTrue(Long tenantId);

    Optional<Category> findByTenant_IdAndCode(Long tenantId, String code);

    boolean existsByTenant_IdAndCode(Long tenantId, String code);
}
