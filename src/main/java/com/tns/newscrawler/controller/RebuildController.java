// src/main/java/com/tns/newscrawler/controller/RebuildController.java
package com.tns.newscrawler.controller;

import com.tns.newscrawler.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RebuildController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/dev/rebuild-redis")
    public String rebuild() {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // tránh gọi đồng thời
                searchService.syncAllData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return "ĐÃ BẮT ĐẦU REBUILD REDIS SEARCH – XEM LOG SPRING BOOT ĐEEEE!!!";
    }
}