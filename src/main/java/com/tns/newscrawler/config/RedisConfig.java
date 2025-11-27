package com.tns.newscrawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfig {

    // 👇 Đây là cái Bean mà SearchService đang tìm kiếm
    @Bean
    public JedisPooled jedisPooled() {
        // Kết nối tới localhost:6379 (Redis Stack)
        return new JedisPooled("localhost", 6379);
    }
}