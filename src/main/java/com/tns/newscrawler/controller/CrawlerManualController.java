package com.tns.newscrawler.controller;

import com.tns.newscrawler.crawler.CrawlerService;
import org.springframework.beans.factory.annotation.Value;  // ✅ đúng
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/crawler")
public class CrawlerManualController {
    private final CrawlerService crawlerService;
    @Value("${crawler.tenant-id}") Long tenantId;
    @Value("${crawler.check-exist:false}") boolean checkExist;

    public CrawlerManualController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @PostMapping("/run-once")
    public String runOnce(@RequestParam(required=false) Long t) {
        Long tid = (t != null) ? t : tenantId;
        int n = crawlerService.crawlTenantOnce(tid, checkExist);
        return "Upsert " + n + " links for tenant " + tid;
    }
}

