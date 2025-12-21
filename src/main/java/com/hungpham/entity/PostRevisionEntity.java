package com.hungpham.entity;

import com.hungpham.common.enums.PostStatusEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "post_revisions")
public class PostRevisionEntity extends BaseUuidEntity{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id", nullable = false)
    private UserEntity editor;

    @Column(name = "revision_no", nullable = false)
    private int revisionNo;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "subtitle", length = 500)
    private String subtitle;

    @Column(name = "slug", nullable = false, length = 300)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PostStatusEnum status;

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

}
