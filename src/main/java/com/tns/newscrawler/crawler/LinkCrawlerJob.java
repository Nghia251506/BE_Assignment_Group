package com.tns.newscrawler.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkCrawlerJob {

    private final com.tns.newscrawler.service.Crawler.LinkCrawlerService linkCrawlerService;

    // ví dụ: 10 phút 1 lần
    @Scheduled(cron = "0 */10 * * * *")
    public void scheduleCrawlAll() {
        int total = linkCrawlerService.crawlAllActiveSources();
        log.info("[LinkCrawlerJob] Scheduled run -> upsert {} links", total);
    }
}
