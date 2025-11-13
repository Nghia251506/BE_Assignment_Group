package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.CrawlLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlLogRepository extends JpaRepository<CrawlLog, Long> {
}
