package com.tns.newscrawler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts",
        indexes = {
                @Index(name="idx_posts_status", columnList="status"),
                @Index(name="idx_posts_category", columnList="category_id"),
                @Index(name="idx_posts_source", columnList="source_id"),
                @Index(name="idx_posts_published_at", columnList="published_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_origin_url", columnNames = {"origin_url"})
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với Source
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    // Liên kết với Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Link gốc dùng để idempotent khi crawler đẩy vào
    @Column(name="origin_url", nullable=false, length=500)
    private String originUrl;

    @Column(name = "canonical_url", length = 500)
    private String canonicalUrl;

    @Column(length=500) private String title;
    @Column(length=500) private String slug;
    @Column(name = "seo_title", length = 500)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "seo_keywords", length = 500)
    private String seoKeywords;
    @Column(columnDefinition="TEXT") private String summary;

    // Lưu cả raw để sau “generate unique”
    @Lob @Column(name="content", columnDefinition="LONGTEXT")
    private String content;

    @Lob @Column(name="content_raw", columnDefinition="LONGTEXT")
    private String contentRaw;

    @Column(length=500) private String thumbnail;

    public enum PostStatus { pending, draft, published, removed }
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private PostStatus status = PostStatus.pending;

    public enum DeleteStatus { Active, Deleted}
    @Enumerated(EnumType.STRING)
    @Column(name="delete_status", nullable=false, length=10)
    private DeleteStatus deleteStatus = DeleteStatus.Active;

    @Column(name="published_at") private LocalDateTime publishedAt;
    @Column(name="view_count") private Integer viewCount = 0;

    @Column(name="created_by") private Long createdBy;  // user id
    @Column(name="updated_by") private Long updatedBy;
    @Column(name="deleted_by") private Long deletedBy;

    @Column(name="created_at", updatable=false) private LocalDateTime createdAt;
    @Column(name="updated_at") private LocalDateTime updatedAt;
    @Column(name="deleted_at") private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        var now = LocalDateTime.now();
        this.createdAt = now; this.updatedAt = now;
        if (status == null) status = PostStatus.pending;
        if (deleteStatus == null) deleteStatus = DeleteStatus.Active;
        if (viewCount == null) viewCount = 0;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
