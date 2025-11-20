package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SourceRepository extends JpaRepository<Source, Long> {

    @Query("select s from Source s " +
            "left join fetch s.category c")
    List<Source> findAllWithCategory();

    // Để crawler lấy danh sách nguồn đang chạy
    @Query(value = "select * from sources where is_active = true", nativeQuery = true)
    List<Source> findByIsActiveTrue();

    // Để tránh trùng, nếu em muốn kiểm tra theo list_url
    boolean existsByListUrl(String listUrl);
}
