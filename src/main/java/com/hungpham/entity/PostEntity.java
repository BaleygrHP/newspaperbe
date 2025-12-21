package com.hungpham.entity;

import com.hungpham.common.enums.PostStatusEnum;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "posts")
public class PostEntity extends BaseUuidEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "subtitle", length = 500)
    private String subtitle;

    @Column(name = "slug", nullable = false, length = 300, unique = true)
    private String slug;

    @Column(name = "content_json", nullable = false, columnDefinition = "JSON")
    private String contentJson;

    @Column(name = "content_md", columnDefinition = "LONGTEXT")
    private String contentMd;

    @Column(name = "content_html", columnDefinition = "LONGTEXT")
    private String contentHtml;

    @Column(name = "content_text", columnDefinition = "LONGTEXT")
    private String contentText;

    @Column(name = "content_version", nullable = false)
    private int contentVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PostStatusEnum status;

    @Column(name = "is_featured", nullable = false)
    private boolean featured;

    @Column(name = "show_on_front_page", nullable = false)
    private boolean showOnFrontPage;

    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    private String coverImageUrl;

    @Column(name = "cover_alt", length = 300)
    private String coverAlt;

    @Column(name = "published_at")
    @CreationTimestamp
    private LocalDateTime publishedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
