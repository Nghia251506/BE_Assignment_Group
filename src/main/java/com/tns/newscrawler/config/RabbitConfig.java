package com.tns.newscrawler.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NEWS = "news.exchange";
    public static final String QUEUE_CONTENT_CRAWL = "news.content.crawl";
    public static final String ROUTING_CONTENT_CRAWL = "content.crawl";

    @Bean
    public Queue contentCrawlQueue() {
        // ✅ durable = true, exclusive = false, autoDelete = false
        // Dùng constructor 2 tham số cho khỏe
        return new Queue(QUEUE_CONTENT_CRAWL, true);
    }

    @Bean
    public DirectExchange newsExchange() {
        return new DirectExchange(EXCHANGE_NEWS);
    }

    @Bean
    public Binding contentCrawlBinding(Queue contentCrawlQueue, DirectExchange newsExchange) {
        return BindingBuilder.bind(contentCrawlQueue)
                .to(newsExchange)
                .with(ROUTING_CONTENT_CRAWL);
    }
}
