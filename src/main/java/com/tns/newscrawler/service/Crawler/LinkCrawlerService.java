package com.tns.newscrawler.service.Crawler;

import com.tns.newscrawler.entity.CrawlLog;
import com.tns.newscrawler.entity.Post;
import com.tns.newscrawler.entity.Source;
import com.tns.newscrawler.repository.PostRepository;
import com.tns.newscrawler.repository.SourceRepository;
import com.tns.newscrawler.messaging.ContentCrawlPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.tns.newscrawler.repository.CrawlLogRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkCrawlerService {

    private final SourceRepository sourceRepository;
    private final PostRepository postRepository;
    private final CrawlLogRepository crawlLogRepository;
    private final ContentCrawlPublisher contentCrawlPublisher;


    /**
     * Crawl tất cả source đang active (toàn hệ thống)
     */
    @Transactional
    public int crawlAllActiveSources() {
        List<Source> sources = sourceRepository.findByIsActiveTrue();
        int total = 0;
        for (Source s : sources) {
            int n = crawlOneSource(s);
            total += n;
            log.info("[LinkCrawler] Tenant={}, Source={} -> Upsert {} links",
                    s.getTenant().getCode(), s.getName(), n);
        }
        return total;
    }

    /**
     * Crawl tất cả source active của một tenant
     */
    @Transactional
    public int crawlActiveSourcesByTenant(Long tenantId) {
        List<Source> sources = sourceRepository.findByTenant_IdAndIsActiveTrue(tenantId);
        int total = 0;
        for (Source s : sources) {
            int n = crawlOneSource(s);
            total += n;
            log.info("[LinkCrawler] Tenant={}, Source={} -> Upsert {} links",
                    s.getTenant().getCode(), s.getName(), n);
        }
        return total;
    }

    /**
     * Crawl 1 source: lấy URL list, parse HTML, lưu link bài viết vào bảng posts
     */
    @Transactional
    public int crawlOneSource(Source source) {
        LocalDateTime start = LocalDateTime.now();
        CrawlLog logEntity = CrawlLog.builder()
                .tenant(source.getTenant())
                .source(source)
                .crawlType(CrawlLog.CrawlType.LINK)
                .triggeredBy(CrawlLog.TriggeredBy.MANUAL) // nếu là job thì SCHEDULED
                .status(CrawlLog.CrawlStatus.SUCCESS)     // tạm set SUCCESS, nếu lỗi thì đổi sau
                .startedAt(start)
                .build();
        crawlLogRepository.save(logEntity);

        Set<String> linkSet = new HashSet<>();
        int inserted = 0;

        try {
            // 1) Log cho dễ debug
            log.info("[LinkCrawler] Start crawl source id={} name={} listUrl={} selector={}",
                    source.getId(),
                    source.getName(),
                    source.getListUrl(),
                    source.getListItemSelector()
            );

            // 2) Tải HTML trang list
            Document doc = Jsoup
                    .connect(source.getListUrl())
                    .userAgent("Mozilla/5.0 (compatible; TNS-NewsCrawler/1.0)")
                    .timeout(10000)
                    .get();

            // 3) Lấy các item theo CSS selector
            Elements items = doc.select(source.getListItemSelector());
            log.info("[LinkCrawler] Found {} elements for selector={}",
                    items.size(), source.getListItemSelector());

            // 4) Duyệt từng item, lấy href, chuẩn hoá URL
            for (Element item : items) {
                String rawHref = item.attr(source.getLinkAttr()); // thường là "href"
                if (!StringUtils.hasText(rawHref)) continue;

                String fullUrl = normalizeUrl(rawHref, source.getBaseUrl());
                if (!StringUtils.hasText(fullUrl)) continue;

                linkSet.add(fullUrl);
            }

            // 5) Ghi log số link tìm được
            logEntity.setTotalFound(linkSet.size());
            log.info("[LinkCrawler] Source id={} name={} -> collected {} unique links",
                    source.getId(), source.getName(), linkSet.size());

            // 6) Upsert vào bảng posts
            for (String url : linkSet) {
                if (postRepository.existsByOriginUrl(url)) {
                    // đã có, bỏ qua
                    continue;
                }
                Post p = Post.builder()
                        .tenant(source.getTenant())
                        .source(source)
                        .category(source.getCategory())
                        .originUrl(url)
                        .build();
                postRepository.save(p);
                inserted++;
                contentCrawlPublisher.sendPostId(p.getId());
            }

            logEntity.setTotalInserted(inserted);
            logEntity.setStatus(CrawlLog.CrawlStatus.SUCCESS);

        } catch (Exception e) {
            logEntity.setStatus(CrawlLog.CrawlStatus.ERROR);
            logEntity.setErrorMessage(e.getMessage());
            log.error("[LinkCrawler] Error when crawling source id={} name={}: {}",
                    source.getId(), source.getName(), e.getMessage(), e);
        } finally {
            logEntity.setFinishedAt(LocalDateTime.now());
            crawlLogRepository.save(logEntity); // update lại log
        }

        return inserted;
    }

    /**
     * Chuẩn hóa URL:
     * - Nếu đã là absolute (bắt đầu bằng http) => giữ nguyên
     * - Nếu là relative => dựa vào baseUrl
     */
    private String normalizeUrl(String href, String baseUrl) {
        try {
            if (!StringUtils.hasText(href)) return null;

            href = href.trim();

            // đã là absolute
            if (href.startsWith("http://") || href.startsWith("https://")) {
                return href;
            }

            // đôi khi có dạng //vnexpress.net/xxx
            if (href.startsWith("//")) {
                URI base = new URI(baseUrl);
                return base.getScheme() + ":" + href;
            }

            // relative path: /thoi-su/abc.html hoặc thoi-su/abc.html
            URI base = new URI(baseUrl);
            URI resolved = base.resolve(href);
            return resolved.toString();
        } catch (Exception e) {
            log.warn("[LinkCrawler] Cannot normalize url href={} base={}", href, baseUrl);
            return null;
        }
    }
}
