package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SourceRepository extends JpaRepository<Source, Long> {

    @Query("select s from Source s " +
            "left join fetch s.category c " +
            "where s.tenant.id = :tenantId")
    List<Source> findByTenantIdWithCategory(@org.springframework.data.repository.query.Param("tenantId") Long tenantId);

//    List<Source> findByTenant_Id(Long tenantId);

    List<Source> findByTenant_IdAndIsActiveTrue(Long tenantId);

    // để crawler lấy danh sách nguồn đang chạy
    @Query(value = "select * from sources where is_active = true", nativeQuery = true)
    List<Source> findByIsActiveTrue();

    // để tránh trùng, nếu em muốn kiểm tra theo list_url
    boolean existsByTenant_IdAndListUrl(Long tenantId, String listUrl);
}
