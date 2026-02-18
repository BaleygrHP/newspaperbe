package com.hungpham.entity;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.common.enums.MediaStorageEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "media_assets")
public class MediaAssetEntity extends BaseUuidEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 20)
    private MediaKindEnum kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage", nullable = false, length = 20)
    private MediaStorageEnum storage;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "storage_key", length = 512)
    private String storageKey;

    @Column(name = "mime_type", nullable = false, length = 120)
    private String mimeType;

    @Column(name = "byte_size", nullable = false)
    private long byteSize;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "alt", length = 300)
    private String alt;

    @Column(name = "title", length = 300)
    private String title;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(length = 255)
    private String caption;

    @Column(length = 120)
    private String location;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(length = 64)
    private String category;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "is_active", nullable = false)
    private boolean active;

}
