package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SourceRepository extends JpaRepository<Source, Long> {

    List<Source> findByTenant_Id(Long tenantId);

    List<Source> findByTenant_IdAndIsActiveTrue(Long tenantId);

    // để crawler lấy danh sách nguồn đang chạy
    @Query(value = "select * form sources where is_active = true", nativeQuery = true)
    List<Source> findByIsActiveTrue();

    // để tránh trùng, nếu em muốn kiểm tra theo list_url
    boolean existsByTenant_IdAndListUrl(Long tenantId, String listUrl);
}
