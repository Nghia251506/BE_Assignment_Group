package com.tns.newscrawler.controller;

import com.tns.newscrawler.entity.redis.PostDoc;
import com.tns.newscrawler.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<PostDoc>> search(@RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(searchService.search(q));
    }

    @PostMapping("/sync-all")
    public ResponseEntity<String> syncAll() {
        searchService.syncAllData();
        return ResponseEntity.ok("Đang đồng bộ dữ liệu ngầm...");
    }
}