package com.tns.newscrawler.crawler;

import ch.qos.logback.classic.Logger;
import com.tns.newscrawler.entity.Source;
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

    // ví dụ: 10 phút 1 lần
    @Scheduled(cron = "0 */10 * * * *") // ✅ mỗi 10 phút
    @Transactional
    public void crawlPendingContentJob() {
        log.info("[ContentCrawlerScheduler] Start job crawl content pending...");

        // tuỳ anh: có Source.status ACTIVE thì filter theo
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
