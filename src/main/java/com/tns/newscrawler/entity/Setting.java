package com.tns.newscrawler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "website_title", nullable = false, length = 255)
    private String websiteTitle;

    @Column(name = "website_description", columnDefinition = "TEXT")
    private String websiteDescription;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "banner_header", columnDefinition = "TEXT")
    private String bannerHeader;

    @Column(name = "banner_sidebar", columnDefinition = "TEXT")
    private String bannerSidebar;

    @Column(name = "google_analytics_tracking_id", length = 50)
    private String googleAnalyticsTrackingId;

    @Column(name = "crawler_frequency_minutes", nullable = false)
    private int crawlerFrequencyMinutes;

    @Column(name = "crawler_new_articles", nullable = false)
    private int crawlerNewArticles;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
