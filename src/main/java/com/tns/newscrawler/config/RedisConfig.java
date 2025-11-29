package com.tns.newscrawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RedisConfig {

//    // 👇 Đây là cái Bean mà SearchService đang tìm kiếm
//    @Bean
//    public JedisPooled jedisPooled() {
//        // Kết nối tới localhost:6379 (Redis Stack)
//        return new JedisPooled("localhost", 6379);
//    }
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public JedisPooled jedisPooled() {
        // In log để debug xem đang kết nối vào đâu
        System.out.println("🔌 [REDIS CONNECT] Host: " + redisHost + " | Port: " + redisPort);

        if (redisPassword != null && !redisPassword.isBlank()) {
            // Trường hợp có mật khẩu (thường là trên Server thật)
            return new JedisPooled(redisHost, redisPort, null, redisPassword);
        } else {
            // Trường hợp không mật khẩu (Localhost)
            return new JedisPooled(redisHost, redisPort);
        }
    }
}