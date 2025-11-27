package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.CrawlLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlLogRepository extends JpaRepository<CrawlLog, Long> {
}
