package com.hungpham.dtos;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.common.enums.MediaStorageEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaAssetDto extends AbstractDto<MediaAssetDto>{
    private String ownerId;
    private MediaKindEnum kind;
    private MediaStorageEnum storage;
    private String url;
    private String storageKey;
    private String mimeType;
    private Long byteSize;
    private Integer width;
    private Integer height;
    private String alt;
    private String title;
    private String originalFileName;
    private String caption;
    private String location;
    private String takenAt;   // yyyy-MM-dd
    private String category;
    private boolean active;

}
