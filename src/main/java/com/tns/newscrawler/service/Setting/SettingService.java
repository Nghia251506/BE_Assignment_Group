package com.tns.newscrawler.service.Setting;

import com.tns.newscrawler.entity.Setting;
import com.tns.newscrawler.repository.SettingRepository;
import org.springframework.stereotype.Service;

@Service
public class SettingService {

    private final SettingRepository settingRepository;

    public SettingService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    // Lấy cấu hình SEO và crawler
    public Setting getSettings() {
        return settingRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new RuntimeException("Settings not found")); // Kiểm tra nếu không có cấu hình
    }

    // Cập nhật cấu hình SEO và crawler
    public Setting updateSettings(Long id, Setting setting) {
        // Có thể thêm logic kiểm tra id hợp lệ hoặc thêm cập nhật cho trường hợp không tìm thấy
        setting.setId(id);  // Đảm bảo ID là hợp lệ
        return settingRepository.save(setting);
    }

    // Tạo cấu hình mới nếu chưa có
    public Setting createSetting(Setting setting) {
        // Kiểm tra nếu có sẵn cấu hình, nếu chưa có thì tạo mới
        if (settingRepository.count() == 0) {  // Kiểm tra có cấu hình hay chưa
            return settingRepository.save(setting);  // Tạo mới nếu chưa có cấu hình
        } else {
            throw new RuntimeException("Settings already exist");
        }
    }
}


