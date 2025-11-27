package com.tns.newscrawler.config;

import com.tns.newscrawler.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IndexLoader implements CommandLineRunner {

    private final SearchService searchService;

    @Override
    public void run(String... args) {
        // Gọi hàm tạo Index thủ công
        searchService.createIndexIfNotExists();
    }
}