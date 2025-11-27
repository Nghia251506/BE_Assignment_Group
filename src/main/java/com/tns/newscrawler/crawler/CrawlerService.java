package com.tns.newscrawler.crawler;

import com.tns.newscrawler.dto.Post.PostCreateRequest;
import com.tns.newscrawler.entity.Source;
import com.tns.newscrawler.repository.SourceRepository;
import com.tns.newscrawler.service.Post.PostService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CrawlerService {

    private final SourceRepository sourceRepo;
    private final PostService postService;

    public CrawlerService(SourceRepository sourceRepo, PostService postService) {
        this.sourceRepo = sourceRepo;
        this.postService = postService;
    }

    /** Chạy 1 lượt cho tất cả nguồn đang active → trả về số bài đã upsert */
    @Transactional(readOnly = true)
    public int crawlOnce(boolean checkExistBeforeUpsert) {
        List<Source> sources = sourceRepo.findByIsActiveTrue();  // Bỏ tenantId
        int total = 0;
        for (Source s : sources) {
            total += crawlOneSource(s, checkExistBeforeUpsert);
            // throttle nhẹ để “lịch sự” với site nguồn
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        }
        return total;
    }

    /** Cào 1 nguồn: lấy link ở listUrl, lọc theo path dựa trên listUrl, lưu DB */
    private int crawlOneSource(Source s, boolean checkExistBeforeUpsert) {
        int saved = 0;
        try {
            // 1) vào trang list
            Document doc = Jsoup.connect(s.getListUrl())
                    .header("User-Agent", "NewsCrawler/1.0 (+https://tns.vn)")
                    .timeout((int) Duration.ofSeconds(15).toMillis())
                    .get();

            String itemSel = s.getListItemSelector();
            if (itemSel == null || itemSel.isBlank()) return 0;

            String linkAttr = (s.getLinkAttr() != null && !s.getLinkAttr().isBlank())
                    ? s.getLinkAttr() : "href";

            // 2) từ listUrl suy ra path prefix để lọc (VD: /thoi-su/)
            String allowedPrefix = extractPathPrefix(s.getBaseUrl(), s.getListUrl());

            Set<String> urls = new HashSet<>();
            for (Element el : doc.select(itemSel)) {
                String absKey = "abs:" + linkAttr;
                String url = el.hasAttr(absKey) ? el.attr(absKey) : el.attr(linkAttr);
                if (url == null || url.isBlank()) continue;

                // chuẩn hóa tuyệt đối nếu cần
                if (!url.startsWith("http")) {
                    String base = s.getBaseUrl() != null ? s.getBaseUrl().replaceAll("/+$","") : "";
                    url = base + "/" + url.replaceAll("^/+","");
                }
                if (!url.startsWith("http")) continue;

                // 3) lọc theo path: chỉ lấy link dưới cùng "mục" của listUrl
                if (allowedPrefix != null && !url.startsWith(allowedPrefix)) continue;

                if (!urls.add(url)) continue;

                // 4) (tùy chọn) bỏ trùng sớm
                if (checkExistBeforeUpsert && postService.existsByOrigin(url)) {
                    continue;
                }

                // 5) lưu DB (pending) – dùng upsertByOrigin cho an toàn
                PostCreateRequest req = new PostCreateRequest();
                req.setSourceId(s.getId());
                req.setCategoryId(s.getCategory().getId());
                req.setOriginUrl(url);
                // title/content sẽ được cập nhật khi crawl chi tiết, ở đây có thể để trống
                postService.upsertByOrigin(req);
                saved++;
            }
        } catch (Exception ex) {
            System.err.println("[crawlOneSource] " + s.getName() + " -> " + ex.getMessage());
        }
        return saved;
    }

    /** Suy ra prefix tuyệt đối để lọc: baseUrl + path chính của listUrl (VD: https://vnexpress.net/thoi-su/) */
    private String extractPathPrefix(String baseUrl, String listUrl) {
        try {
            java.net.URI list = new java.net.URI(listUrl);
            String schemeHost = list.getScheme() + "://" + list.getHost();
            String path = list.getPath(); // vd: /thoi-su, /the-thao/bong-da
            if (path == null || path.isBlank() || "/".equals(path)) return schemeHost + "/";

            // lấy segment gốc (vd: /thoi-su/)
            String[] segs = path.split("/");
            if (segs.length >= 1) {
                String first = segs.length > 1 ? segs[1] : "";
                if (!first.isBlank()) return schemeHost + "/" + first + "/";
            }
            return schemeHost + "/";
        } catch (Exception e) {
            // fallback nếu listUrl không parser được
            if (baseUrl != null && !baseUrl.isBlank()) return baseUrl.replaceAll("/+$","") + "/";
            return null;
        }
    }
}
