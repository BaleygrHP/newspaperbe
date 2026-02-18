package com.hungpham.requests.media;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.common.enums.MediaStorageEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMediaByUrlRequest {
    private MediaKindEnum kind = MediaKindEnum.IMAGE;
    private MediaStorageEnum storage = MediaStorageEnum.URL;

    private String url;
    private String mimeType;
    private Long byteSize;
    private Integer width;
    private Integer height;

    private String alt;
    private String title;


    private String caption;
    private String location;
    private String takenAt;    // yyyy-MM-dd
    private String category;

    private String fileHash;
}
