package com.hungpham.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaBinaryDto {
    private boolean redirect;
    private String redirectUrl;
    private byte[] content;
    private long contentLength;
    private String mimeType;
    private String fileName;
    private String eTag;
    private String cacheControl;
    private boolean nosniff;
    private boolean acceptsRanges;
}
