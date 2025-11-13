package com.tns.newscrawler.controller;

import com.tns.newscrawler.service.Crawler.ContentCrawlerService;
import com.tns.newscrawler.service.Crawler.LinkCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/crawler")
@RequiredArgsConstructor
public class CrawlerManualController {

    private final LinkCrawlerService linkCrawlerService;
    private final ContentCrawlerService contentCrawlerService;


    // Crawl tất cả source active
    @PostMapping("/links/all")
    public String crawlAll() {
        int total = linkCrawlerService.crawlAllActiveSources();
        return "Upsert " + total + " links for all active sources";
    }

    // Crawl theo tenant
    @PostMapping("/links/by-tenant")
    public String crawlByTenant(@RequestParam("tenantId") Long tenantId) {
        int total = linkCrawlerService.crawlActiveSourcesByTenant(tenantId);
        return "Upsert " + total + " links for tenant " + tenantId + ": " + total + " links";
    }
    // ✅ NEW: Crawl content cho các post pending của 1 source
    @PostMapping("/content/by-source")
    public String crawlContentBySource(
            @RequestParam Long sourceId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        int ok = contentCrawlerService.crawlPendingBySource(sourceId, limit);
        return "Crawled content for " + ok + " posts of source " + sourceId;
    }
}
