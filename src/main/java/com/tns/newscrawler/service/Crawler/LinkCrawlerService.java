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

    @Transactional
    public int crawlAllActiveSources() {
        List<Source> sources = sourceRepository.findByIsActiveTrue();
        int total = 0;
        for (Source s : sources) {
            int n = crawlOneSource(s);
            total += n;
            log.info("[LinkCrawler] Source={} -> Upsert {} links",
                    s.getName(), n);
        }
        return total;
    }

    @Transactional
    public int crawlOneSource(Source source) {
        LocalDateTime start = LocalDateTime.now();
        CrawlLog logEntity = CrawlLog.builder()
                .source(source)
                .crawlType(CrawlLog.CrawlType.LINK)
                .triggeredBy(CrawlLog.TriggeredBy.MANUAL)
                .status(CrawlLog.CrawlStatus.SUCCESS)
                .startedAt(start)
                .build();
        crawlLogRepository.save(logEntity);

        Set<String> linkSet = new HashSet<>();
        int inserted = 0;

        try {
            log.info("[LinkCrawler] Start crawl source id={} name={} listUrl={} selector={}",
                    source.getId(), source.getName(), source.getListUrl(), source.getListItemSelector());

            Document doc = Jsoup
                    .connect(source.getListUrl())
                    .userAgent("Mozilla/5.0 (compatible; TNS-NewsCrawler/1.0)")
                    .timeout(10000)
                    .get();

            Elements items = doc.select(source.getListItemSelector());
            log.info("[LinkCrawler] Found {} elements for selector={}", items.size(), source.getListItemSelector());

            for (Element item : items) {
                String rawHref = item.attr(source.getLinkAttr());
                if (!StringUtils.hasText(rawHref)) continue;

                String fullUrl = normalizeUrl(rawHref, source.getBaseUrl());
                if (!StringUtils.hasText(fullUrl)) continue;

                linkSet.add(fullUrl);
            }

            logEntity.setTotalFound(linkSet.size());
            log.info("[LinkCrawler] Source id={} name={} -> collected {} unique links",
                    source.getId(), source.getName(), linkSet.size());

            for (String url : linkSet) {
                if (postRepository.existsByOriginUrl(url)) continue;
                Post p = Post.builder()
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
            log.error("[LinkCrawler] Error crawling source id={} name={}: {}", source.getId(), source.getName(), e.getMessage(), e);
        } finally {
            logEntity.setFinishedAt(LocalDateTime.now());
            crawlLogRepository.save(logEntity);
        }

        return inserted;
    }

    private String normalizeUrl(String href, String baseUrl) {
        try {
            if (!StringUtils.hasText(href)) return null;

            href = href.trim();

            if (href.startsWith("http://") || href.startsWith("https://")) {
                return href;
            }

            if (href.startsWith("//")) {
                URI base = new URI(baseUrl);
                return base.getScheme() + ":" + href;
            }

            URI base = new URI(baseUrl);
            URI resolved = base.resolve(href);
            return resolved.toString();
        } catch (Exception e) {
            log.warn("[LinkCrawler] Cannot normalize url href={} base={}", href, baseUrl);
            return null;
        }
    }
}
