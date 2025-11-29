package com.tns.newscrawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.JedisPooled;
import java.net.URI;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.url:redis://localhost:6379}")
    private String redisUrl;

    @Bean
    public JedisPooled jedisPooled() throws Exception {
        URI uri = new URI(redisUrl);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 6379 : uri.getPort();

        System.out.println("Redis URL: " + redisUrl);
        System.out.println("Connecting to → " + host + ":" + port);

        String username = null;
        String password = null;

        if (uri.getUserInfo() != null) {
            String[] parts = uri.getUserInfo().split(":", 2);
            username = parts[0];
            if (parts.length > 1) {
                password = parts[1];
            }
        }

        // Jedis 4.x chỉ có 2 constructor chính hỗ trợ ACL:
        if (username != null && password != null && !password.isEmpty()) {
            System.out.println("Using ACL: username = " + username);
            // Constructor đúng cho Jedis 4.x + ACL
            return new JedisPooled(host, port, username, password);
        }
        else if (password != null && !password.isEmpty()) {
            System.out.println("Using legacy password auth");
            return new JedisPooled(host, port, Boolean.parseBoolean(password));
        }
        else {
            System.out.println("No authentication");
            return new JedisPooled(host, port);
        }
    }
}