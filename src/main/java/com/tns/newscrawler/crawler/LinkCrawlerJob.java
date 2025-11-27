package com.tns.newscrawler.crawler;

import com.tns.newscrawler.entity.Setting;  // Sửa thành đúng tên entity
import com.tns.newscrawler.service.Crawler.LinkCrawlerService;
import com.tns.newscrawler.repository.SettingRepository;  // Sử dụng SettingRepository để lấy cấu hình
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkCrawlerJob {

    private final LinkCrawlerService linkCrawlerService;
    private final SettingRepository settingRepository; // Inject SettingRepository để lấy cấu hình

    // ví dụ: 10 phút 1 lần
    @Scheduled(cron = "0 */10 * * * *") // Default mỗi 10 phút
    public void scheduleCrawlAll() {
        // Lấy cấu hình từ bảng Setting
        Optional<Setting> settingOptional = settingRepository.findTopByOrderByIdDesc();
        if (settingOptional.isPresent()) {
            Setting setting = settingOptional.get();
            int frequencyMinutes = setting.getCrawlerFrequencyMinutes();  // Giả sử bạn có field này trong Setting

            // Cập nhật cron expression dựa vào frequencyMinutes từ bảng Setting
            String cronExpression = "0 0 */" + frequencyMinutes + " * * *";  // Cập nhật cron mỗi frequencyMinutes phút

            log.info("[LinkCrawlerJob] Cron job scheduled every {} minutes with cron expression: {}",
                    frequencyMinutes, cronExpression);

            // Tiến hành crawl tất cả nguồn active
            int total = linkCrawlerService.crawlAllActiveSources();
            log.info("[LinkCrawlerJob] Scheduled run -> upsert {} links", total);

        } else {
            log.error("[LinkCrawlerJob] No settings found. Cron job not scheduled.");
        }
    }
}
