package com.tns.newscrawler.service.Crawler;

import com.tns.newscrawler.entity.CrawlLog;
import com.tns.newscrawler.entity.Post;
import com.tns.newscrawler.entity.Source;
import com.tns.newscrawler.repository.CrawlLogRepository;
import com.tns.newscrawler.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentCrawlerService {

    private final PostRepository postRepository;
    private final CrawlLogRepository crawlLogRepository;

    @Transactional
    public void crawlOnePostById(Long postId) throws Exception {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        // dùng lại logic crawl 1 bài
        crawlOnePostContent(post);

        // update status, save
        post.setStatus(Post.PostStatus.draft); // hoặc published
        postRepository.save(post);
    }

    /**
     * Crawl content cho các post pending của 1 source (giới hạn limit post)
     */
    @Transactional
    public int crawlPendingBySource(Long sourceId, int limit) {
        Page<Post> page = postRepository.findBySource_IdAndStatus(
                sourceId,
                Post.PostStatus.pending,
                PageRequest.of(0, limit)
        );
        if (page.isEmpty()) {
            log.info("[ContentCrawler] No pending posts for source {}", sourceId);
            return 0;
        }

// ✅ dùng get(0) vì đã check isEmpty ở trên
        Post first = page.getContent().get(0);
        Source source = first.getSource();

        LocalDateTime start = LocalDateTime.now();
        CrawlLog logEntity = CrawlLog.builder()
                .tenant(source.getTenant())
                .source(source)
                .crawlType(CrawlLog.CrawlType.CONTENT)
                .triggeredBy(CrawlLog.TriggeredBy.MANUAL) // nếu là job thì đổi SCHEDULED
                .status(CrawlLog.CrawlStatus.SUCCESS)
                .startedAt(start)
                .totalFound(page.getNumberOfElements())
                .build();
        crawlLogRepository.save(logEntity);

        int success = 0;
        StringBuilder errorBuilder = new StringBuilder();

        for (Post post : page.getContent()) {
            try {
                crawlOnePostContent(post);
                post.setStatus(Post.PostStatus.draft); // hoặc published tuỳ rule
                postRepository.save(post);
                success++;
            } catch (Exception e) {
                log.error("[ContentCrawler] Error crawl content for post id={} url={}: {}",
                        post.getId(), post.getOriginUrl(), e.getMessage(), e);
                // gom lỗi ngắn gọn vào log
                errorBuilder.append("postId=")
                        .append(post.getId())
                        .append(": ")
                        .append(e.getMessage())
                        .append("\n");
            }
        }

        logEntity.setTotalInserted(success); // tái dùng field này = số post crawl thành công
        if (success == page.getNumberOfElements()) {
            logEntity.setStatus(CrawlLog.CrawlStatus.SUCCESS);
        } else if (success == 0) {
            logEntity.setStatus(CrawlLog.CrawlStatus.ERROR);
        } else {
            logEntity.setStatus(CrawlLog.CrawlStatus.PARTIAL);
        }

        if (!errorBuilder.isEmpty()) {
            logEntity.setErrorMessage(errorBuilder.toString());
        }
        logEntity.setFinishedAt(LocalDateTime.now());
        crawlLogRepository.save(logEntity);

        log.info("[ContentCrawler] Done source={} -> success {}/{} posts",
                sourceId, success, page.getNumberOfElements());

        return success;
    }

    /**
     * Crawl content cho 1 post (dùng originUrl + selector trong Source)
     */
    private void crawlOnePostContent(Post post) throws Exception {
        Source source = post.getSource();
        if (source == null) {
            throw new IllegalStateException("Post " + post.getId() + " has no source");
        }

        String url = post.getOriginUrl();
        log.info("[ContentCrawler] Fetching detail url={} (postId={})", url, post.getId());

        Document doc = Jsoup
                .connect(url)
                .userAgent("Mozilla/5.0 (compatible; TNS-NewsCrawler/1.0)")
                .timeout(10000)
                .get();

        // --- TITLE ---
        String title = null;
        if (StringUtils.hasText(source.getTitleSelector())) {
            Elements els = doc.select(source.getTitleSelector());
            if (!els.isEmpty()) {
                title = els.text();
            }
        }

        // --- CONTENT RAW (HTML) ---
        String contentHtml = null;
        if (StringUtils.hasText(source.getContentSelector())) {
            Elements els = doc.select(source.getContentSelector());
            if (!els.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Element el : els) {
                    sb.append(el.outerHtml());
                }
                contentHtml = sb.toString();
            }
        }

        // --- THUMBNAIL ---
        String thumbnail = null;
        if (StringUtils.hasText(source.getThumbnailSelector())) {
            Element img = doc.select(source.getThumbnailSelector()).first();
            if (img != null) {
                String rawSrc = img.hasAttr("src") ? img.attr("src") : img.attr("data-src");
                thumbnail = normalizeUrl(rawSrc, source.getBaseUrl());
            }
        }

        // --- AUTHOR ---
        String author = null;
        if (StringUtils.hasText(source.getAuthorSelector())) {
            Elements els = doc.select(source.getAuthorSelector());
            if (!els.isEmpty()) {
                author = els.text();
            }
        }

        // --- SUMMARY (text, cắt ngắn) ---
        String summary = null;
        String contentText = null;
        if (contentHtml != null) {
            contentText = Jsoup.parse(contentHtml).text();
            if (contentText.length() > 300) {
                summary = contentText.substring(0, 300) + "...";
            } else {
                summary = contentText;
            }
        }

        // Gán vào Post
        if (StringUtils.hasText(title)) {
            post.setTitle(title);
        }
        if (contentHtml != null) {
            post.setContentRaw(contentHtml);
            // tuỳ em: content = HTML sạch hoặc text, ở đây anh để text
            post.setContent(contentText);
        }
        if (StringUtils.hasText(thumbnail)) {
            post.setThumbnail(thumbnail);
        }
        if (StringUtils.hasText(summary)) {
            post.setSummary(summary);
        }
        // author hiện chưa có field riêng trong Post, nếu em muốn thì thêm cột sau

        // có thể set publishedAt = now() nếu muốn
        if (post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
    }

    /**
     * Reuse normalize URL giống link crawler
     */
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
            log.warn("[ContentCrawler] Cannot normalize url href={} base={}", href, baseUrl);
            return null;
        }
    }
}
