package com.tns.newscrawler.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_tags",
        uniqueConstraints = @UniqueConstraint(name="uk_post_tag", columnNames = {"post_id","tag_id"}),
        indexes = {
                @Index(name="idx_post_tags_post", columnList="post_id"),
                @Index(name="idx_post_tags_tag", columnList="tag_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostTag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="post_id", nullable=false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="tag_id", nullable=false)
    private Tag tag;
}
