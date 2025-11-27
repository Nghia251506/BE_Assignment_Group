package com.tns.newscrawler.crawler;

import ch.qos.logback.classic.Logger;
import com.tns.newscrawler.entity.Setting;
import com.tns.newscrawler.entity.Source;
import com.tns.newscrawler.repository.SettingRepository;
import com.tns.newscrawler.repository.SourceRepository;
import com.tns.newscrawler.service.Crawler.ContentCrawlerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentCrawlerJob {

    private final ContentCrawlerService contentCrawlerService;
    private final SourceRepository sourceRepository;
    private final SettingRepository settingsRepository;  // Inject Settings repository

    // ví dụ: cài đặt cronjob chạy theo thời gian cài đặt trong Settings
    @Scheduled(cron = "0 0 */1 * * *")  // Default schedule mỗi giờ
    @Transactional
    public void crawlPendingContentJob() {
        log.info("[ContentCrawlerScheduler] Start job crawl content pending...");

        // Lấy cấu hình từ bảng settings
        Setting settings = settingsRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new RuntimeException("Settings not found"));

        // Thời gian từ bảng settings (có thể lấy thời gian phút từ bảng)
        int frequencyMinutes = settings.getCrawlerFrequencyMinutes();
        String cronExpression = "0 0 */" + frequencyMinutes + " * * *";  // Cập nhật cron dựa vào frequency

        // In thông báo cron job đã được cài đặt
        log.info("[ContentCrawlerScheduler] Cron job scheduled every {} minutes with cron expression: {}",
                frequencyMinutes, cronExpression);

        // Tương tự, fetch các source đang hoạt động
        List<Source> sources = sourceRepository.findAll();

        int totalSuccess = 0;
        for (Source source : sources) {
            try {
                // ví dụ mỗi source cào tối đa 20 bài pending / 1 lần job
                int success = contentCrawlerService.crawlPendingBySource(source.getId(), 20);
                totalSuccess += success;

                log.info("[ContentCrawlerScheduler] sourceId={} -> success {} posts",
                        source.getId(), success);
            } catch (Exception e) {
                log.error("[ContentCrawlerScheduler] Error crawl for sourceId={}: {}",
                        source.getId(), e.getMessage(), e);
            }
        }

        log.info("[ContentCrawlerScheduler] Done job. Total success posts = {}", totalSuccess);
    }
}

