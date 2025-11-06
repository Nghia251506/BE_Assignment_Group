package com.tns.newscrawler.crawler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CrawlerJob {

    private final CrawlerService crawlerService;

    @Value("${crawler.tenant-id}")
    private Long tenantId;

    @Value("${crawler.check-exist:false}")
    private boolean checkExistBeforeUpsert;

    public CrawlerJob(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    // cron lấy từ properties: crawler.cron
    @Scheduled(cron = "${crawler.cron}")
    public void run() {
        for (String raw : System.getProperty("crawler.tenant-id","1").split(",")) {
            Long tid = Long.valueOf(raw.trim());
            int n = crawlerService.crawlTenantOnce(tid, checkExistBeforeUpsert);
            System.out.println("[CrawlerJob] tenant=" + tid + " upsert " + n + " links");
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
    }
}
