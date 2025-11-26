package com.tns.newscrawler.service;

import com.google.gson.Gson;
import com.tns.newscrawler.entity.Post;
import com.tns.newscrawler.entity.redis.PostDoc;
import com.tns.newscrawler.repository.jpa.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.*;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final JedisPooled jedis;
    private final PostRepository postRepository;
    private final Gson gson = new Gson();

    private static final String INDEX_NAME = "post-idx";
    private static final String PREFIX = "post:";

    // Tạo index nếu chưa tồn tại – SỬA: HASH thay vì JSON để Jedis hỗ trợ native
    public void createIndexIfNotExists() {
        try {
            jedis.ftInfo(INDEX_NAME);
            log.info("Index {} đã tồn tại", INDEX_NAME);
            return;
        } catch (Exception e) {
            log.info("Chưa có index → Tạo mới {}", INDEX_NAME);
        }

        try {
            Schema schema = new Schema()
                    .addTextField("title", 5.0)
                    .addTextField("summary", 1.0)
                    .addTagField("categoryName")
                    .addTagField("sourceName")
                    .addNumericField("publishedAt");

            IndexDefinition rule = new IndexDefinition(IndexDefinition.Type.HASH)  // ← HASH – native, không lỗi
                    .setPrefixes(PREFIX);

            jedis.ftCreate(INDEX_NAME, IndexOptions.defaultOptions().setDefinition(rule), schema);
            log.info("Tạo index {} thành công!", INDEX_NAME);
        } catch (Exception e) {
            log.error("Lỗi tạo index: {}", e.getMessage(), e);
        }
    }

    // Đồng bộ 1 bài – SỬA: LƯU HASH + JSON STRING để FE đọc
    public void syncPostToRedis(Post post) {
        try {
            PostDoc doc = new PostDoc();
            doc.setId(String.valueOf(post.getId()));
            doc.setTitle(post.getTitle() != null ? post.getTitle() : "");
            doc.setSummary(post.getSummary() != null ? post.getSummary() : "");
            doc.setThumbnail(post.getThumbnail());
            doc.setSlug(post.getSlug());
            doc.setCategoryName(post.getCategory() != null ? post.getCategory().getName() : "");
            doc.setSourceName(post.getSource() != null ? post.getSource().getName() : "");
            doc.setPublishedAt(post.getPublishedAt() != null
                    ? post.getPublishedAt().toEpochSecond(ZoneOffset.UTC)
                    : System.currentTimeMillis() / 1000);

            String key = PREFIX + post.getId();
            String json = gson.toJson(doc);

            // LƯU HASH CHO INDEX + STRING CHO FE ĐỌC (KHÔNG LỖI TYPE)
            jedis.del(key); // Xóa cũ nếu có
            Map<String, String> hashFields = new HashMap<>();
            hashFields.put("title", doc.getTitle());
            hashFields.put("summary", doc.getSummary());
            hashFields.put("thumbnail", doc.getThumbnail() != null ? doc.getThumbnail() : "");
            hashFields.put("slug", doc.getSlug() != null ? doc.getSlug() : "");
            hashFields.put("categoryName", doc.getCategoryName());
            hashFields.put("sourceName", doc.getSourceName());
            hashFields.put("publishedAt", String.valueOf(doc.getPublishedAt()));
            jedis.hset(key, hashFields);  // ← HASH CHO INDEX
            jedis.hset(key, "jsonPayload", json);  // ← STRING CHO FE ĐỌC JSON

            log.info("Synced {} → {}", key, doc.getTitle());
        } catch (Exception e) {
            log.error("Lỗi sync post {}: {}", post.getId(), e.getMessage());
        }
    }

    // TÌM KIẾM – SỬA: LẤY TỪ HASH + JSON STRING
    public List<PostDoc> search(String keyword) {
        List<PostDoc> results = new ArrayList<>();
        createIndexIfNotExists();

        try {
            String queryStr;
            if (keyword == null || keyword.trim().isEmpty()) {
                queryStr = "*";
            } else {
                String clean = keyword.trim()
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replaceAll("\\s+", " ");

                queryStr = String.format("(@title:*%s* | @summary:*%s*)", clean, clean);
            }

            log.info("Redis Query → {}", queryStr);

            Query query = new Query(queryStr)
                    .limit(0, 50)
                    .setSortBy("publishedAt", true); // true = DESC

            SearchResult sr = jedis.ftSearch(INDEX_NAME, query);
            log.info("Tìm thấy {} kết quả trong Redis", sr.getTotalResults());

            for (Document doc : sr.getDocuments()) {
                // LẤY JSON TỪ HASH FIELD "jsonPayload"
                String json = (String) doc.get("jsonPayload");
                if (json != null) {
                    PostDoc postDoc = gson.fromJson(json, PostDoc.class);
                    results.add(postDoc);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi search Redis: {}", e.getMessage(), e);
        }

        log.info("Trả về {} kết quả cho FE", results.size());
        return results;
    }

    // Xóa index cũ
    public void dropIndex() {
        try {
            jedis.ftDropIndex(INDEX_NAME);
            log.info("Đã xóa index {}", INDEX_NAME);
        } catch (Exception e) {
            log.warn("Chưa có index để xóa");
        }
    }

    // Rebuild toàn bộ – gọi khi cần
    public void syncAllData() {
        log.info("Bắt đầu rebuild Redis Search...");
        dropIndex();
        createIndexIfNotExists();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        List<Post> posts = postRepository.findAll();
        log.info("Đang sync {} bài viết...", posts.size());

        int count = 0;
        for (Post post : posts) {
            syncPostToRedis(post);
            if (++count % 50 == 0) {
                log.info("Đã sync {}/{}", count, posts.size());
            }
        }

        log.info("HOÀN TẤT! Đã đồng bộ {} bài vào Redis Search", posts.size());
    }
}