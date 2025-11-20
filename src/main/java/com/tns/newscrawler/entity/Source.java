package com.tns.newscrawler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---- category ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // tên hiển thị cho admin
    @Column(nullable = false, length = 255)
    private String name;

    // domain gốc: https://vnexpress.net
    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    // link trang list để crawl: https://vnexpress.net/thoi-su
    @Column(name = "list_url", nullable = false, length = 500)
    private String listUrl;

    // selector để tìm từng item bài trong trang list
    @Column(name = "list_item_selector", nullable = false, length = 255)
    private String listItemSelector;

    // thuộc tính để lấy link: href, data-href...
    @Column(name = "link_attr", nullable = false, length = 50)
    private String linkAttr;

    // selector trang chi tiết
    @Column(name = "title_selector", length = 255)
    private String titleSelector;

    @Column(name = "content_selector", length = 255)
    private String contentSelector;

    @Column(name = "thumbnail_selector", length = 255)
    private String thumbnailSelector;

    @Column(name = "author_selector", length = 255)
    private String authorSelector;

    // bật/tắt crawl
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // ghi chú nhanh cho admin
    @Column(length = 500)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
        if (this.linkAttr == null) this.linkAttr = "href";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
