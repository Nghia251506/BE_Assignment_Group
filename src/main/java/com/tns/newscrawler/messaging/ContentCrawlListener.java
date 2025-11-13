package com.tns.newscrawler.messaging;

import com.tns.newscrawler.config.RabbitConfig;
import com.tns.newscrawler.service.Crawler.ContentCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentCrawlListener {

    private final ContentCrawlerService contentCrawlerService;

    @RabbitListener(queues = RabbitConfig.QUEUE_CONTENT_CRAWL)
    public void handleContentCrawl(Long postId) {
        log.info("[RabbitMQ] Received postId={} from queue {}", postId, RabbitConfig.QUEUE_CONTENT_CRAWL);

        try {
            contentCrawlerService.crawlOnePostById(postId);
            log.info("[ContentCrawler] Done crawl content for postId={}", postId);
        } catch (Exception e) {
            log.error("[ContentCrawler] Error when crawling postId={}: {}", postId, e.getMessage(), e);
            // tuỳ em: có thể gửi sang dead-letter queue, hoặc log thôi là đủ cho assignment
        }
    }
}
