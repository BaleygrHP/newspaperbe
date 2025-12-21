package com.hungpham.entity;

import com.hungpham.common.enums.PostMediaRoleEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "post_media")
public class PostMediaEntity {

    @EmbeddedId
    private PostMediaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mediaId")
    @JoinColumn(name = "media_id", nullable = false)
    private MediaAssetEntity media;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_role", nullable = false, length = 20)
    private PostMediaRoleEnum mediaRole;

    @Column(name = "position")
    private Integer position;
}
