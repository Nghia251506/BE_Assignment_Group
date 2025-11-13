package com.tns.newscrawler.messaging;

import com.tns.newscrawler.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentCrawlPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendPostId(Long postId) {
        log.info("[RabbitMQ] Send postId={} to queue {}", postId, RabbitConfig.QUEUE_CONTENT_CRAWL);
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NEWS,
                RabbitConfig.ROUTING_CONTENT_CRAWL,
                postId
        );
    }
}
