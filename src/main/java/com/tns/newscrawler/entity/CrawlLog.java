package com.tns.newscrawler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawl_logs",
        indexes = {
                @Index(name = "idx_crawl_logs_source", columnList = "source_id"),
                @Index(name = "idx_crawl_logs_status", columnList = "status"),
                @Index(name = "idx_crawl_logs_started", columnList = "started_at")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CrawlLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với Source
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    public enum CrawlType { LINK, CONTENT, BOTH }
    @Enumerated(EnumType.STRING)
    @Column(name = "crawl_type", nullable = false, length = 20)
    private CrawlType crawlType;

    public enum TriggeredBy { MANUAL, SCHEDULED, RABBITMQ }
    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", nullable = false, length = 20)
    private TriggeredBy triggeredBy;

    public enum CrawlStatus { SUCCESS, ERROR, PARTIAL }
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CrawlStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "total_found")
    private Integer totalFound;

    @Column(name = "total_inserted")
    private Integer totalInserted;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (startedAt == null) startedAt = createdAt;
    }
}
