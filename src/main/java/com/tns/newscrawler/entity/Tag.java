package com.tns.newscrawler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tags",
        indexes = @Index(name="idx_tags_tenant", columnList = "tenant_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // multi-tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 120)
    private String slug;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (slug == null && name != null) {
            slug = name.trim().toLowerCase().replaceAll("\\s+", "-");
        }
    }
}
