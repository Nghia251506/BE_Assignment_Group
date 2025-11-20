package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findTopByOrderByIdDesc(); // Để lấy cài đặt đầu tiên nếu không có phân biệt tenant
}
